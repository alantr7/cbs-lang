package com.github.alantr7.codebots.cbslang.high.parser.ast.expressions;

public class Compare implements Operand {

    public Operand left;

    public Operand right;

    public byte operation;

    public static final byte EQUALS = 20;
    public static final byte NOT_EQUALS = 21;
    public static final byte LESS_THAN = 22;
    public static final byte GREATER_THAN = 23;
    public static final byte LESS_EQUALS = 24;
    public static final byte GREATER_EQUALS = 25;

    public Compare(Operand left, byte operation, Operand right) {
        this.left = left;
        this.operation = operation;
        this.right = right;
    }

}
