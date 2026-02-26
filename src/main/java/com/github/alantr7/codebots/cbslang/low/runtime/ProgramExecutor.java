package com.github.alantr7.codebots.cbslang.low.runtime;

import com.github.alantr7.codebots.cbslang.low.runtime.memory.Data;
import com.github.alantr7.codebots.cbslang.low.runtime.memory.DataType;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.Context;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.ExternalFunction;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.Module;

import java.util.AbstractMap;
import java.util.Map;

@SuppressWarnings("all")
public class ProgramExecutor {

    private final Program program;

    public ProgramExecutor(Program program) {
        this.program = program;
    }

    void handleDEFC(String[] instruction) {
        assert instruction[0].equals("defc");

        DataType<?> dataType = switch (instruction[1]) {
            case "int"      -> DataType.INT;
            case "flt"    -> DataType.FLOAT;
            case "str"      -> DataType.STRING;
            default -> null;
        };

        Object value;

        if (dataType.equals(DataType.INT))
            value = Integer.parseInt(instruction[2]);

        else if (dataType.equals(DataType.FLOAT))
            value = Float.parseFloat(instruction[2]);

        else if (dataType.equals(DataType.STRING))
            value = instruction[2];

        else value = null;

        int esp = (int) program.state.REGISTER_ESP.getValue();
        program.state.locate(esp).setValue((DataType<? super Object>) dataType, value);
        program.state.REGISTER_ESP.setValue(DataType.INT, esp + 1);
    }

    void handleIMPF(String[] instruction) {
        assert instruction[0].equals("impf");
        program.state.IMPORTS.add(new AbstractMap.SimpleEntry<>(instruction[1], instruction[2]));
    }

    void handleMOV(String[] instruction) {
        assert instruction[0].equals("mov");

        String destinationName = instruction[1];
        String sourceName = instruction[2];

        Data destination = program.state.locate(new String[] { destinationName });
        Data source = program.state.locate(new String[] { sourceName });

        destination.setValue((DataType<Object>) source.getDataType(), source.getValue());
    }

    void handlePUSH(String[] instruction) {
        assert instruction[0].equals("push");

        Data value = program.state.locate(new String[] { instruction[1] });

        int esp = (int) program.state.REGISTER_ESP.getValue();

        program.state.locate(esp++).setValue((DataType<Object>) value.getDataType(), value.getValue());

        program.state.REGISTER_ESP.setValue(DataType.INT, esp);
    }

    void handlePOP(String[] instruction) {
        assert instruction[0].equals("pop") && instruction.length <= 2;

        Data top = program.state.locate((int) program.state.REGISTER_ESP.getValue() - 1);
        if (instruction.length == 2) {
            program.state.locate(new String[] { instruction[1] }).setValue((DataType<? super Object>) top.getDataType(), top.getValue());
        }

        program.state.REGISTER_ESP.setValue(DataType.INT, (int) program.state.REGISTER_ESP.getValue() - 1);
    }

    void handleCMP(String[] instruction) {
        assert instruction[0].equals("cmp");

        Data left = program.state.locate(new String[] { instruction[1] });
        Data right = program.state.locate(new String[] { instruction[2] });


        int rightVal = (int) right.getValue();
        int leftVal = (int) left.getValue();

        int diff = leftVal - rightVal;
        program.state.REGISTER_CMP.setValue(DataType.INT, diff == 0 ? 0 : diff < 0 ? -1 : 1);
    }

    public static final byte JMP = -2;
    public static final byte JE = 0;
    public static final byte JNE = -3;
    public static final byte JL = 3;
    public static final byte JLE = -1;
    public static final byte JG = 4;
    public static final byte JGE = 1;
    public static final byte CALL = 2;
    void handleJMP(String[] instruction, byte condition) {
        assert condition >= JNE && condition <= JG;

        if (condition != JMP && condition != JNE && condition != JLE && condition != JL && condition != JGE && condition != JG && condition != CALL && condition != (int) program.state.REGISTER_CMP.getValue())
            return;

        if (condition == JNE && 0 == (int) program.state.REGISTER_CMP.getValue())
            return;

        if (condition == JLE && (int) program.state.REGISTER_CMP.getValue() == 1)
            return;

        if (condition == JL && (int) program.state.REGISTER_CMP.getValue() != -1)
            return;

        if (condition == JGE && (int) program.state.REGISTER_CMP.getValue() == -1)
            return;

        if (condition == JG && (int) program.state.REGISTER_CMP.getValue() != 1)
            return;

        if (condition == CALL) {
            handlePUSH(new String[] { "push", String.valueOf(program.state.REGISTER_EIP.getValue()) });

            // check if function is imported
            if (instruction[1].charAt(0) == '.') {
                Map.Entry<String, String> imp = program.state.IMPORTS.get(Integer.parseInt(instruction[1].substring(1)));
                Module module = program.moduleRepository.getModule(imp.getKey());
                ExternalFunction handler = module.getFunction(imp.getValue());

                int ebp = program.state.REGISTER_ESP.getValueAs(DataType.INT) - 1;
                int argumentsCount = (int) program.state.locate(ebp - 1).getValue();

                Data[] arguments = new Data[argumentsCount];
                for (int i = -argumentsCount - 1; i < -1; i++) {
                    arguments[i + argumentsCount + 1] = program.state.locate(ebp + i);
                }

                program.state.EXTERNAL_FUNCTION = Integer.parseInt(instruction[1].substring(1));
                program.state.EXTERNAL_FUNCTION_CONTEXT = new Context(arguments);

                Data returnValue = handler.handle(program.state.EXTERNAL_FUNCTION_CONTEXT);
                if (returnValue != null) {
                    program.state.REGISTER_RAX.setValue((DataType) returnValue.getDataType(), returnValue.getValue());
                }
            }
        }

        String label = instruction[1] + ":";
        for (int i = 0; i < program.instructions.length; i++) {
            if (program.instructions[i].length != 1 || !program.instructions[i][0].endsWith(":"))
                continue;

            if (program.instructions[i][0].equals(label)) {
                program.state.REGISTER_EIP.setValue(DataType.INT, i + 1);
                return;
            }
        }
    }

    void handleRET(String[] instruction) {
        assert instruction[0].equals("ret");

        // where to go back?
        handleMATH(new String[] { "add", "ebp", "2" }, ProgramExecutor.ADD);
        handleMOV(new String[] { "mov", "esp", "ebp" });
        handlePOP(new String[] { "pop", "ebp" });
        handlePOP(new String[] { "pop", "eip" });
    }

    public static final byte ADD = 0;
    public static final byte SUB = 1;
    public static final byte MUL = 2;
    public static final byte DIV = 3;
    public static final byte MOD = 4;
    void handleMATH(String[] instruction, byte op) {
        assert op >= ADD && op <= MOD;

        Data destination = program.state.locate(new String[] { instruction[1] });
        Data source = program.state.locate(new String[] { instruction[2] });

        float current = destination.getDataType() == DataType.FLOAT ? (float) destination.getValue() : (int) destination.getValue();
        float sourceVal = source.getDataType() == DataType.FLOAT ? (float) source.getValue() : (int) source.getValue();

        float result = switch (op) {
            case ADD -> current + sourceVal;
            case SUB -> current - sourceVal;
            case MUL -> current * sourceVal;
            case DIV -> current / sourceVal;
            case MOD -> current % sourceVal;
            default -> 0;
        };

//        System.out.println(current + " " + op + " " + sourceVal + " = " + result);

        DataType<?> resultType = (destination.getDataType() == DataType.FLOAT || source.getDataType() == DataType.FLOAT ? DataType.FLOAT : DataType.INT);
        Number resultCasted;

        if (resultType == DataType.INT) {
            resultCasted = (int) result;
        } else {
            resultCasted = (float) result;
        }

        destination.setValue((DataType<Object>) resultType, resultCasted);
    }

    void handleCAT(String[] instruction) {
        assert instruction[0].equals("cat");

        Data destination = program.state.locate(new String[] { instruction[1] });
        Data source = program.state.locate(new String[] { instruction[2] });

        String current = String.valueOf(destination.getValue());
        String sourceVal = String.valueOf(source.getValue());

        destination.setValue(DataType.STRING, current + sourceVal);
    }

}
