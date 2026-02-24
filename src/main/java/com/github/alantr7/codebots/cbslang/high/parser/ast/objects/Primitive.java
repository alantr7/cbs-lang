package com.github.alantr7.codebots.cbslang.high.parser.ast.objects;

import lombok.Getter;

public class Primitive extends Type {

    public static final Primitive INT = new Primitive("int");

    public static final Primitive FLOAT = new Primitive("float");

    public static final Primitive STRING = new Primitive("string");

    public static final Primitive VOID = new Primitive("void");

    @Getter
    private final String name;

    public Primitive(String name) {
        this.name = name;
    }

}
