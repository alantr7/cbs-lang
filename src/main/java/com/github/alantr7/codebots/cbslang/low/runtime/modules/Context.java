package com.github.alantr7.codebots.cbslang.low.runtime.modules;

import com.github.alantr7.codebots.cbslang.low.runtime.memory.Data;
import lombok.Getter;
import lombok.Setter;

public class Context {

    @Getter
    Data[] arguments;

    @Getter @Setter
    boolean recall;

    @Getter
    Data[] memory = new Data[8];

    public Context(Data[] arguments) {
        this.arguments = arguments;
    }

}
