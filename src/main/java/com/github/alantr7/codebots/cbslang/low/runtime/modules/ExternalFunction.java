package com.github.alantr7.codebots.cbslang.low.runtime.modules;

import com.github.alantr7.codebots.cbslang.low.runtime.memory.Data;
import com.github.alantr7.codebots.cbslang.low.runtime.memory.DataType;
import lombok.Getter;

public abstract class ExternalFunction {

    @Getter
    private final DataType<?>[] parameterTypes;

    @Getter
    private final DataType<?> returnType;

    public ExternalFunction(DataType<?> returnType, DataType<?>... parameterTypes) {
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
    }

    public abstract Data handle(Context context);

}
