package com.github.alantr7.codebots.cbslang.high.parser.ast.objects;

public class FunctionSignature {

    public String module;

    public String name;

    public Type returnType;

    public Type[] parameterTypes;

    public FunctionSignature(String module, String name, Type returnType, Type[] parameterTypes) {
        this.module = module;
        this.name = name;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
    }

}
