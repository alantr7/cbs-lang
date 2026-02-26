package com.github.alantr7.codebots.cbslang.high.parser.ast.objects;

import com.github.alantr7.codebots.cbslang.low.runtime.memory.DataType;

import java.util.HashMap;
import java.util.Map;

public abstract class Type {

    private static final Map<DataType<?>, Type> adapters = new HashMap<>();

    public Type(DataType<?> corresponding) {
        adapters.put(corresponding, this);
    }

    public static Type adapt(DataType<?> corresponding) {
        return adapters.get(corresponding);
    }

}
