package com.github.alantr7.codebots.cbslang.low.runtime;

import com.github.alantr7.bytils.buffer.ByteArrayReader;
import com.github.alantr7.bytils.buffer.ByteArrayWriter;
import com.github.alantr7.codebots.cbslang.low.runtime.memory.DataType;
import com.github.alantr7.codebots.cbslang.low.runtime.memory.Data;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.Context;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ProgramState {

    final Data REGISTER_EBP = new Data();

    final Data REGISTER_EIP = new Data();

    final Data REGISTER_ESP = new Data();

    final Data REGISTER_EAX = new Data();

    final Data REGISTER_EBX = new Data();

    final Data REGISTER_CMP = new Data();

    final Data REGISTER_RAX = new Data();

    final Data[] MEMORY = new Data[256];

    final List<Map.Entry<String, String>> IMPORTS = new LinkedList<>();

    int EXTERNAL_FUNCTION;

    Context EXTERNAL_FUNCTION_CONTEXT;

    // Initialize registers with zeros
    {
        REGISTER_EIP.setValue(DataType.INT, 0);
        REGISTER_EBP.setValue(DataType.INT, 0);
        REGISTER_ESP.setValue(DataType.INT, 0);
        REGISTER_EBX.setValue(DataType.INT, 0);
        REGISTER_EAX.setValue(DataType.INT, 0);
        REGISTER_CMP.setValue(DataType.INT, 0);
        REGISTER_RAX.setValue(DataType.INT, 0);
    }

    public void dump() {
        System.out.println("Registers:");
        System.out.printf(" eip: \t%10s\t%s\n", REGISTER_EIP.getDataType().getTypeName(), REGISTER_EIP.getValue());
        System.out.printf(" ebp: \t%10s\t%s\n", REGISTER_EBP.getDataType().getTypeName(), REGISTER_EBP.getValue());
        System.out.printf(" esp: \t%10s\t%s\n", REGISTER_ESP.getDataType().getTypeName(), REGISTER_ESP.getValue());
        System.out.printf(" eax: \t%10s\t%s\n", REGISTER_EAX.getDataType().getTypeName(), REGISTER_EAX.getValue());
        System.out.printf(" ebx: \t%10s\t%s\n", REGISTER_EBX.getDataType().getTypeName(), REGISTER_EBX.getValue());
        System.out.printf(" rax: \t%10s\t%s\n", REGISTER_RAX.getDataType().getTypeName(), REGISTER_RAX.getValue());
        System.out.printf(" cmp: \t%10s\t%s\n", REGISTER_CMP.getDataType().getTypeName(), REGISTER_CMP.getValue());
        System.out.println();

        System.out.println("Memory:");
        int i = 0;
        for (; i < MEMORY.length; i++) {
            if (MEMORY[i] == null)
                continue;

            System.out.printf(" %3d\t%10s\t%s\n", i, MEMORY[i].getDataType().getTypeName(), MEMORY[i].getValue());
        }
        System.out.printf(" (%d remaining slots were never used)", MEMORY.length - i);
    }

    /**
     *
     * @param operand Operand can be a register, value of a register, or direct address by a number.
     *                Examples respectively: EBP, [EBP], [5].
     *                Direct address supports basic arithmetic operations: [5+3], [EBP - 4]
     */
    public @NotNull Data locate(String[] operand) {
        Data data;

        // Address to a value
        if (operand[0].charAt(0) == '[' && operand[0].charAt(operand[0].length() - 1) == ']') {
            if (operand[0].contains("+") || operand[0].contains("-")) {
                String[] operands = operand[0].substring(1, operand[0].length() - 1).split("(?=[+-])|(?<=[+-])");
                int value = (int) locate(new String[] { operands[0].trim() }).getValue();

                for (int i = 1; i < operands.length; i += 2) {
                    int right = (int) locate(new String[] { operands[i + 1].trim() }).getValue();
                    if (operands[i].equals("+"))
                        value += right;
                    else value -= right;
                }

                return locate(value);
            }
            Data pointer = locate(new String[] { operand[0].substring(1, operand[0].length() - 1) });
            return locate((int) pointer.getValue());
        }

        if (operand[0].matches("-?\\d+")) {
            data = new Data();
            data.setValue(DataType.INT, Integer.parseInt(operand[0]));

            return data;
        }

        return getRegistry(operand[0]);
    }

    public @NotNull Data locate(int address) {
        return MEMORY[address] != null ? MEMORY[address] : (MEMORY[address] = new Data());
    }

    public Data[] getMemory() {
        return MEMORY;
    }

    private Data getRegistry(String name) {
        return switch(name) {
            case "eip" -> REGISTER_EIP;
            case "esp" -> REGISTER_ESP;
            case "ebp" -> REGISTER_EBP;
            case "eax" -> REGISTER_EAX;
            case "ebx" -> REGISTER_EBX;
            case "cmp" -> REGISTER_CMP;
            case "rax" -> REGISTER_RAX;
            default -> null;
        };
    }

    public byte[] serialize() {
        ByteArrayWriter buffer = new ByteArrayWriter(512);

        // Save imports
        buffer.writeU1(IMPORTS.size());
        for (Map.Entry<String, String> imp : IMPORTS) {
            buffer.writeString(imp.getKey());
            buffer.writeString(imp.getValue());
        }

        // Save registers
        serializeData(buffer, REGISTER_EIP);
        serializeData(buffer, REGISTER_ESP);
        serializeData(buffer, REGISTER_EBP);
        serializeData(buffer, REGISTER_EAX);
        serializeData(buffer, REGISTER_EBX);
        serializeData(buffer, REGISTER_CMP);
        serializeData(buffer, REGISTER_RAX);

        // Save memory
        for (Data data : MEMORY) {
            serializeData(buffer, data);
        }

        // Save context for external call
        if (EXTERNAL_FUNCTION_CONTEXT != null) {
            buffer.writeU1(1);
            buffer.writeU1(EXTERNAL_FUNCTION);

            buffer.writeU1(EXTERNAL_FUNCTION_CONTEXT.getArguments().length);
            for (Data data : EXTERNAL_FUNCTION_CONTEXT.getArguments()) {
                serializeData(buffer, data);
            }

            for (Data data : EXTERNAL_FUNCTION_CONTEXT.getMemory()) {
                serializeData(buffer, data);
            }

            buffer.writeU1(EXTERNAL_FUNCTION_CONTEXT.isRecall() ? 1 : 0);
        } else {
            buffer.writeU1(0);
        }

        return buffer.getBuffer();
    }

    private static void serializeData(ByteArrayWriter buffer, Data data) {
        if (data == null) {
            buffer.writeU1(0);
            return;
        }

        buffer.writeU1(data.getDataType().getSerializationId());
        switch (data.getDataType().getSerializationId()) {
            case 1 -> buffer.writeInt(data.getValueAs(DataType.INT));
            case 2 -> buffer.writeLong(data.getValueAs(DataType.LONG));
            case 3 -> buffer.writeInt(Float.floatToIntBits(data.getValueAs(DataType.FLOAT)));
            case 4 -> buffer.writeString(data.getValueAs(DataType.STRING));
        }
    }

    public static ProgramState deserialize(Program program, ByteArrayReader buffer) {
        ProgramState state = new ProgramState();

        // Load imports
        int importCount = buffer.readU1();
        for (int i = 0; i < importCount; i++) {
            state.IMPORTS.add(new AbstractMap.SimpleEntry<>(buffer.readString(), buffer.readString()));
        }

        // Load registers
        state.REGISTER_EIP.copyFrom(deserializeData(buffer));
        state.REGISTER_ESP.copyFrom(deserializeData(buffer));
        state.REGISTER_EBP.copyFrom(deserializeData(buffer));
        state.REGISTER_EAX.copyFrom(deserializeData(buffer));
        state.REGISTER_EBX.copyFrom(deserializeData(buffer));
        state.REGISTER_CMP.copyFrom(deserializeData(buffer));
        state.REGISTER_RAX.copyFrom(deserializeData(buffer));

        // Load memory
        for (int i = 0; i < state.MEMORY.length; i++) {
            Data data = deserializeData(buffer);
            if (data != null) {
                state.MEMORY[i] = data;
            }
        }

        // Load context for external call
        if (buffer.readU1() == 1) {
            state.EXTERNAL_FUNCTION = buffer.readU1();

            int argumentCount = buffer.readU1();
            Data[] arguments = new Data[argumentCount];
            for (int i = 0; i < argumentCount; i++) {
                arguments[i] = deserializeData(buffer);
            }

            Context context = new Context(program, arguments);
            for (int i = 0; i < 8; i++) {
                Data data = deserializeData(buffer);
                context.getMemory()[i] = data;
            }

            context.setRecall(buffer.readU1() == 1);
            state.EXTERNAL_FUNCTION_CONTEXT = context;
        }

        return state;
    }

    private static Data deserializeData(ByteArrayReader buffer) {
        int id = buffer.readU1();
        return switch (id) {
            case 1 -> new Data(DataType.INT, buffer.readInt());
            case 2 -> new Data(DataType.LONG, buffer.readLong());
            case 3 -> new Data(DataType.FLOAT, Float.intBitsToFloat(buffer.readInt()));
            case 4 -> new Data(DataType.STRING, buffer.readString());
            default -> null;
        };
    }

}
