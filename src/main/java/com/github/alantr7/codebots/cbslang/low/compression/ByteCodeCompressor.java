package com.github.alantr7.codebots.cbslang.low.compression;

import com.github.alantr7.bytils.buffer.ByteArrayWriter;
import com.github.alantr7.codebots.cbslang.low.ByteCode;

public class ByteCodeCompressor {

    private final ByteArrayWriter buffer = new ByteArrayWriter();

    public void compress(String[][] tokenized) {
        for (String[] line : tokenized) {
            switch (line[0]) {
                case "defc"             -> handleDEFC(line);
                case "impf"             -> handleIMPF(line);
                case "mov"              -> handleMOV(line);
                case "push"             -> handlePUSH(line);
                case "pop"              -> handlePOP(line);
                case "cmp"              -> handleCMP(line);
                case "jmp", "call",
                     "je", "jne",
                     "jl", "jle",
                     "jg", "jge"        -> handleJMP(line);
                case "ret"              -> buffer.writeU1(ByteCode.RET);
                case "add", "sub",
                     "mul", "div",
                     "mod"              -> handleMATH(line);
                case "cflti"            -> buffer.writeU1(ByteCode.CFLTI);
                case "ciflt"            -> buffer.writeU1(ByteCode.CIFLT);
                case "cat"              -> handleCAT(line);
                case "halt"             -> buffer.writeU1(ByteCode.HALT);
                case "exit"             -> buffer.writeU1(ByteCode.EXIT);
                default -> {
                    if (line[0].endsWith(":")) handleDEFL(line);
                }
            }
        }
    }

    private void handleDEFC(String[] instruction) {
        buffer.writeU1(ByteCode.DEFC);
        switch (instruction[1]) {
            case "int" -> {
                buffer.writeU1(ByteCode.DEFC_INT);
                buffer.writeInt(Integer.parseInt(instruction[2]));
            }
            case "flt" -> {
                buffer.writeU1(ByteCode.DEFC_FLT);
                buffer.writeFloat(Float.parseFloat(instruction[2]));
            }
            case "str" -> {
                buffer.writeU1(ByteCode.DEFC_STR);
                buffer.writeString(instruction[2]);
            }
        }
    }

    private void handleIMPF(String[] instruction) {
        buffer.writeU1(ByteCode.IMPF);
        buffer.writeShortString(instruction[1]);
        buffer.writeShortString(instruction[2]);
    }

    private void handleDEFL(String[] instruction) {
        buffer.writeU1(ByteCode.DEFL);
        buffer.writeShortString(instruction[0].substring(0, instruction[0].length() - 1));
    }

    private void handleMOV(String[] instruction) {
        buffer.writeU1(ByteCode.MOV);
        handleValueAccess(instruction[1]);
        handleValueAccess(instruction[2]);
    }

    private void handlePUSH(String[] instruction) {
        buffer.writeU1(ByteCode.PUSH);
        handleValueAccess(instruction[1]);
    }

    private void handlePOP(String[] instruction) {
        if (instruction.length == 1) {
            buffer.writeU1(ByteCode.POP_VOID);
        } else {
            buffer.writeU1(ByteCode.POP);
            handleValueAccess(instruction[1]);
        }
    }

    private void handleCMP(String[] instruction) {
        buffer.writeU1(ByteCode.CMP);
        handleValueAccess(instruction[1]);
        handleValueAccess(instruction[2]);
    }

    private void handleJMP(String[] instruction) {
        buffer.writeU1(switch (instruction[0]) {
            case "jmp"  -> ByteCode.JMP;
            case "je"   -> ByteCode.JE;
            case "jne"  -> ByteCode.JNE;
            case "jl"   -> ByteCode.JL;
            case "jle"  -> ByteCode.JLE;
            case "jg"   -> ByteCode.JG;
            case "jge"  -> ByteCode.JGE;
            case "call" -> ByteCode.CALL;
            default -> 0;
        });
        buffer.writeShortString(instruction[1]);
    }

    private void handleMATH(String[] instruction) {
        buffer.writeU1(switch (instruction[0]) {
            case "add"  -> ByteCode.ADD;
            case "sub"  -> ByteCode.SUB;
            case "mul"  -> ByteCode.MUL;
            case "div"  -> ByteCode.DIV;
            case "mod"  -> ByteCode.MOD;
            default -> 0;
        });
        handleValueAccess(instruction[1]);
        handleValueAccess(instruction[2]);
    }

    private void handleCAT(String[] instruction) {
        buffer.writeU1(ByteCode.CAT);
        handleValueAccess(instruction[1]);
        handleValueAccess(instruction[2]);
    }

    private void handleValueAccess(String token) {
        // address access
        if (token.charAt(0) == '[' && token.charAt(token.length() - 1) == ']') {
            token = token.substring(1, token.length() - 1);
            int operatorPos = token.indexOf('+');
            if (operatorPos == -1) operatorPos = token.indexOf('-');

            if (operatorPos == -1) {
                buffer.writeU1(ByteCode.ACC_MEM);
                handleValueAccessReference(token);
            } else {
                buffer.writeU1(ByteCode.ACC_MEM_OFF);
                buffer.writeU1(token.charAt(operatorPos) == '+' ? 1 : 2);
                handleValueAccessReference(token.substring(0, operatorPos));
                handleValueAccessReference(token.substring(operatorPos + 1));
            }
            return;
        }

        // value access
        buffer.writeU1(ByteCode.ACC_DIR);
        handleValueAccessReference(token);
    }

    private void handleValueAccessReference(String token) {
        if (token.matches("-?\\d+")) {
            buffer.writeU1(ByteCode.REF_CONST);
            buffer.writeInt(Integer.parseInt(token));
        } else {
            buffer.writeU1(ByteCode.REF_REG);
            buffer.writeU1(getRegistry(token));
        }
    }

    private static byte getRegistry(String name) {
        return switch (name) {
            case "eip" -> ByteCode.REG_EIP;
            case "ebp" -> ByteCode.REG_EBP;
            case "esp" -> ByteCode.REG_ESP;
            case "eax" -> ByteCode.REG_EAX;
            case "ebx" -> ByteCode.REG_EBX;
            case "cmp" -> ByteCode.REG_CMP;
            case "rax" -> ByteCode.REG_RAX;
            default -> 0;
        };
    }

    public byte[] getOutput() {
        return buffer.getBytes();
    }

}
