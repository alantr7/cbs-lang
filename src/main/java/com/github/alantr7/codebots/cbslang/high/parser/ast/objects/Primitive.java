package com.github.alantr7.codebots.cbslang.high.parser.ast.objects;

import com.github.alantr7.codebots.cbslang.low.runtime.memory.DataType;
import lombok.Getter;

public class Primitive extends Type {

    public static final Primitive INT = new Primitive(DataType.INT, "int");

    public static final Primitive FLOAT = new Primitive(DataType.FLOAT, "float");

    public static final Primitive STRING = new Primitive(DataType.STRING, "string");

    public static final Primitive VOID = new Primitive(DataType.VOID, "void");

    @Getter
    private final String name;

    public Primitive(DataType<?> corresponding, String name) {
        super(corresponding);
        this.name = name;
    }

}
