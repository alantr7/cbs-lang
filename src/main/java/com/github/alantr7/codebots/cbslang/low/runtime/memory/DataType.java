package com.github.alantr7.codebots.cbslang.low.runtime.memory;

import lombok.Getter;

public class DataType<T> {

    @Getter
    private final Class<T> typeClass;

    @Getter
    private final int serializationId;

    @Getter
    private final String typeName;

    public static final DataType<Integer>   INT = new DataType<>(Integer.class, 1, "INT");
    public static final DataType<Long>      LONG = new DataType<>(Long.class, 2, "LONG");
    public static final DataType<Float>     FLOAT = new DataType<>(Float.class, 3, "FLOAT");
    public static final DataType<String>    STRING = new DataType<>(String.class, 4, "STRING");


    // Used only for function parameter and return types!

    public static final DataType<Object>    PRIMITIVE = new DataType<>(Object.class, 0, "PRIMITIVE");
    public static final DataType<Object>    VOID = new DataType<>(Object.class, 0, "VOID");

    public DataType(Class<T> typeClass, int serializationId, String name) {
        this.typeClass = typeClass;
        this.serializationId = serializationId;
        this.typeName = name;
    }

}
