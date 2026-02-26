package com.github.alantr7.codebots.cbslang.low.runtime;

import com.github.alantr7.codebots.cbslang.low.runtime.memory.DataType;
import com.github.alantr7.codebots.cbslang.low.runtime.memory.Data;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.Context;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class ProgramState {

    final Data REGISTER_EBP = new Data();

    final Data REGISTER_EIP = new Data();

    final Data REGISTER_ESP = new Data();

    final Data REGISTER_EAX = new Data();

    final Data REGISTER_EBX = new Data();

    final Data REGISTER_CMP = new Data();

    final Data REGISTER_RAX = new Data();

    @Getter
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

}
