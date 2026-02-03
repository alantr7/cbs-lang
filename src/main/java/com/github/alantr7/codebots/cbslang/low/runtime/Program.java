package com.github.alantr7.codebots.cbslang.low.runtime;

import com.github.alantr7.codebots.cbslang.low.runtime.memory.Data;
import com.github.alantr7.codebots.cbslang.low.runtime.memory.DataType;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.ExternalFunction;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.Module;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.ModuleRepository;
import lombok.Getter;

import java.util.Map;

public class Program {

    final ModuleRepository moduleRepository;

    final String[][] instructions;

    final ProgramExecutor executor = new ProgramExecutor(this);

    @Getter
    final ProgramState state = new ProgramState();

    public Program(String[][] instructions, ModuleRepository repository) {
        this.moduleRepository = repository;
        this.instructions = instructions;
    }

    public void execute() {
        while ((int) state.REGISTER_EIP.getValue() < instructions.length)
            next();
    }

    public void next() {
        if (state.EXTERNAL_FUNCTION_CONTEXT != null) {
            if (state.EXTERNAL_FUNCTION_CONTEXT.isRecall()) {
                Map.Entry<String, String> imp = state.IMPORTS.get(state.EXTERNAL_FUNCTION);
                Module module = moduleRepository.getModule(imp.getKey());
                ExternalFunction handler = module.getFunction(imp.getValue());

                Data returnValue = handler.handle(state.EXTERNAL_FUNCTION_CONTEXT);
                if (returnValue != null) {
                    state.EXTERNAL_FUNCTION_CONTEXT = null;
                }
            } else {
                state.EXTERNAL_FUNCTION_CONTEXT = null;
            }
            return;
        }

        int eip = (int) state.REGISTER_EIP.getValue();
        String[] instruction = instructions[eip];
        state.REGISTER_EIP.setValue(DataType.INT, eip + 1);

        String command = instruction[0];

        if (command.endsWith(":") && instruction.length == 1)
            return;

        try {
//            System.out.println(Arrays.toString(instruction));
            switch (command) {
                case "defc" -> executor.handleDEFC(instruction);
                case "impf" -> executor.handleIMPF(instruction);

                case "mov" -> executor.handleMOV(instruction);
                case "push" -> executor.handlePUSH(instruction);
                case "pop" -> executor.handlePOP(instruction);

                case "jmp" -> executor.handleJMP(instruction, ProgramExecutor.JMP);
                case "je" -> executor.handleJMP(instruction, ProgramExecutor.JE);
                case "jne" -> executor.handleJMP(instruction, ProgramExecutor.JNE);
                case "jl" -> executor.handleJMP(instruction, ProgramExecutor.JL);
                case "jg" -> executor.handleJMP(instruction, ProgramExecutor.JG);
                case "jle" -> executor.handleJMP(instruction, ProgramExecutor.JLE);
                case "jge" -> executor.handleJMP(instruction, ProgramExecutor.JGE);
                case "call" -> executor.handleJMP(instruction, ProgramExecutor.CALL);

                case "ret" -> executor.handleRET(instruction);
                case "exit" -> state.REGISTER_EIP.setValue(DataType.INT, instructions.length);

                case "cmp" -> executor.handleCMP(instruction);
                case "add" -> executor.handleMATH(instruction, ProgramExecutor.ADD);
                case "sub" -> executor.handleMATH(instruction, ProgramExecutor.SUB);
                case "mul" -> executor.handleMATH(instruction, ProgramExecutor.MUL);
                case "div" -> executor.handleMATH(instruction, ProgramExecutor.DIV);
                case "mod" -> executor.handleMATH(instruction, ProgramExecutor.MOD);

                case "dump" -> state.dump();
            }
        } catch (Exception e) {
            System.out.println("Failed at: " + String.join(", ", instructions[(int) state.REGISTER_EIP.getValue() - 1]));

            e.printStackTrace();
            state.REGISTER_EIP.setValue(DataType.INT, instructions.length);
        }
    }

}
