package com.github.alantr7.codebots.cbslang.low.runtime.modules;

import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.FunctionSignature;
import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.Type;
import com.github.alantr7.codebots.cbslang.low.runtime.memory.Data;
import com.github.alantr7.codebots.cbslang.low.runtime.memory.DataType;
import lombok.Getter;

import java.util.Arrays;

public abstract class ExternalFunction {

    @Getter
    private final Module module;

    @Getter
    private final String name;

    @Getter
    private final DataType<?>[] parameterTypes;

    @Getter
    private final DataType<?> returnType;

    public ExternalFunction(Module module, String name, DataType<?> returnType, DataType<?>... parameterTypes) {
        this.module = module;
        this.name = name;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
    }

    public abstract Data handle(Context context);

    public FunctionSignature createSignature() {
        return new FunctionSignature(
          module != null ? module.getName() : null,
          name,
          Type.adapt(returnType),
          Arrays.stream(parameterTypes)
            .map(Type::adapt)
            .toArray(Type[]::new)
        );
    }

}
