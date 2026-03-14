package com.github.alantr7.codebots.cbslang.low.runtime.modules;

import com.github.alantr7.codebots.cbslang.low.runtime.Program;
import com.github.alantr7.codebots.cbslang.low.runtime.memory.Data;
import com.github.alantr7.codebots.cbslang.low.runtime.memory.DataType;
import lombok.Getter;
import lombok.Setter;

public class Context {

    @Getter
    Program program;

    @Getter
    Data[] arguments;

    @Getter @Setter
    boolean recall;

    @Getter
    Data[] memory = new Data[8];

    public Context(Program program, Data[] arguments) {
        this.program = program;
        this.arguments = arguments;
    }

    public <T> T getArgumentAs(int index, DataType<T> type) {
        return arguments[index].getValueAs(type);
    }

}
