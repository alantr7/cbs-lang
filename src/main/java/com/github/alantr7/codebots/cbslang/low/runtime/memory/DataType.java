package com.github.alantr7.codebots.cbslang.low.runtime.memory;

import lombok.Getter;

public class DataType<T> {

    @Getter
    private final Class<T> typeClass;

    @Getter
    private final String typeName;

    public static final DataType<Integer>   INT = new DataType<>(Integer.class, "INT");
    public static final DataType<Long>      LONG = new DataType<>(Long.class, "LONG");
    public static final DataType<Float>     FLOAT = new DataType<>(Float.class, "FLOAT");
    public static final DataType<String>    STRING = new DataType<>(String.class, "STRING");


    // Used only for function parameter and return types!

    public static final DataType<Object>    PRIMITIVE = new DataType<>(Object.class, "PRIMITIVE");
    public static final DataType<Object>    VOID = new DataType<>(Object.class, "VOID");

    public DataType(Class<T> typeClass, String name) {
        this.typeClass = typeClass;
        this.typeName = name;
    }

}
