package com.github.alantr7.codebots.cbslang.low.compression;

import com.github.alantr7.bytils.buffer.ByteArrayReader;
import com.github.alantr7.codebots.cbslang.low.ByteCode;

import java.util.Arrays;

public class ByteCodeDecompressor {

    private final ByteArrayReader reader;

    public ByteCodeDecompressor(byte[] compressed) {
        this.reader = new ByteArrayReader(compressed);
    }

    public String[] decompress() {
        String[] tokenized = new String[2048];
        int index = 0;
        while (reader.hasNext()) {
            int instruction = reader.readU1();
            StringBuilder line = new StringBuilder();
            switch (instruction) {
                case ByteCode.DEFC          -> handleDEFC(line);
                case ByteCode.IMPF          -> handleIMPF(line);
                case ByteCode.DEFL          -> handleDEFL(line);
                case ByteCode.MOV           -> handleMOV(line);
                case ByteCode.PUSH          -> handlePUSH(line);
                case ByteCode.POP_VOID,
                     ByteCode.POP           -> handlePOP(instruction, line);
                case ByteCode.CMP           -> handleCMP(line);
                case ByteCode.JMP,
                     ByteCode.JE,
                     ByteCode.JNE,
                     ByteCode.JL,
                     ByteCode.JLE,
                     ByteCode.JG,
                     ByteCode.JGE,
                     ByteCode.CALL          -> handleJMP((byte) instruction, line);
                case ByteCode.RET           -> line.append("ret");
                case ByteCode.ADD,
                     ByteCode.SUB,
                     ByteCode.MUL,
                     ByteCode.DIV,
                     ByteCode.MOD           -> handleMATH((byte) instruction, line);
                case ByteCode.CFLTI         -> line.append("cflti");
                case ByteCode.CIFLT         -> line.append("ciflt");
                case ByteCode.CAT           -> handleCAT(line);
                case ByteCode.HALT          -> line.append("halt");
                case ByteCode.EXIT          -> line.append("exit");
            };

            if (!line.isEmpty()) {
                tokenized[index] = line.toString().trim();
            }

            index++;
        }

        return Arrays.copyOf(tokenized, index);
    }

    private void handleDEFC(StringBuilder builder) {
        builder.append("defc ");
        switch (reader.readU1()) {
            case ByteCode.DEFC_INT -> {
                builder.append("int, ");
                builder.append(reader.readInt());
            }
            case ByteCode.DEFC_FLT -> {
                builder.append("flt, ");
                builder.append(reader.readFloat());
            }
            case ByteCode.DEFC_STR -> {
                builder.append("str, ");
                builder.append(reader.readString());
            }
        }
    }

    private void handleIMPF(StringBuilder builder) {
        builder.append("impf ").append(reader.readShortString()).append(", ").append(reader.readShortString());
    }

    private void handleDEFL(StringBuilder builder) {
        builder.append(reader.readShortString()).append(":");
    }

    private void handleMOV(StringBuilder builder) {
        builder.append("mov ");
        handleValueAccess(builder);
        builder.append(", ");
        handleValueAccess(builder);
    }

    private void handlePUSH(StringBuilder builder) {
        builder.append("push ");
        handleValueAccess(builder);
    }

    private void handlePOP(int type, StringBuilder builder) {
        builder.append("pop ");
        if (type == ByteCode.POP) {
            handleValueAccess(builder);
        }
    }

    private void handleCMP(StringBuilder builder) {
        builder.append("cmp ");
        handleValueAccess(builder);
        builder.append(", ");
        handleValueAccess(builder);
    }

    private void handleJMP(byte type, StringBuilder builder) {
        switch (type) {
            case ByteCode.JMP   -> builder.append("jmp ");
            case ByteCode.JE    -> builder.append("je ");
            case ByteCode.JNE   -> builder.append("jne ");
            case ByteCode.JL    -> builder.append("jl ");
            case ByteCode.JLE   -> builder.append("jle ");
            case ByteCode.JG    -> builder.append("jg ");
            case ByteCode.JGE   -> builder.append("jge ");
            case ByteCode.CALL  -> builder.append("call ");
        }
        builder.append(reader.readShortString());
    }

    private void handleMATH(byte type, StringBuilder builder) {
        switch (type) {
            case ByteCode.ADD   -> builder.append("add ");
            case ByteCode.SUB   -> builder.append("sub ");
            case ByteCode.MUL   -> builder.append("mul ");
            case ByteCode.DIV   -> builder.append("div ");
            case ByteCode.MOD   -> builder.append("mod ");
        }
        handleValueAccess(builder);
        builder.append(", ");
        handleValueAccess(builder);
    }

    private void handleCAT(StringBuilder builder) {
        builder.append("cat ");
        handleValueAccess(builder);
        builder.append(", ");
        handleValueAccess(builder);
    }

    private void handleValueAccess(StringBuilder builder) {
        int access = reader.readU1();

        // address access
        if (access == ByteCode.ACC_MEM_OFF) {
            byte operator = (byte) reader.readU1();
            builder.append("[");
            handleValueAccessReference(builder);
            builder.append(operator == 1 ? "+" : "-");
            handleValueAccessReference(builder);
            builder.append("]");
            return;
        }

        else if (access == ByteCode.ACC_MEM) {
            builder.append("[");
            handleValueAccessReference(builder);
            builder.append("]");
        }

        // value access
        else if (access == ByteCode.ACC_DIR) {
            handleValueAccessReference(builder);
        }
    }

    private void handleValueAccessReference(StringBuilder builder) {
        int reference = reader.readU1();
        if (reference == ByteCode.REF_CONST) {
            builder.append(reader.readInt());
        } else if (reference == ByteCode.REF_REG) {
            builder.append(getRegistry((byte) reader.readU1()));
        }
    }

    private static String getRegistry(byte id) {
        return switch (id) {
            case ByteCode.REG_EIP -> "eip";
            case ByteCode.REG_EBP -> "ebp";
            case ByteCode.REG_ESP -> "esp";
            case ByteCode.REG_EAX -> "eax";
            case ByteCode.REG_EBX -> "ebx";
            case ByteCode.REG_CMP -> "cmp";
            case ByteCode.REG_RAX -> "rax";
            default -> "";
        };
    }

}
