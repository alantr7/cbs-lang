package com.github.alantr7.codebots.cbslang.high.parser.ast.expressions;

import com.github.alantr7.codebots.cbslang.high.parser.ast.statements.Statement;

public class Unary implements Statement, Operand {

    public Access operand;

    public byte operation;

    public Unary(Access operand, byte operation) {
        this.operand = operand;
        this.operation = operation;
    }

    public static final byte PREFIX_INCREMENT = 40;
    public static final byte POSTFIX_INCREMENT = 41;
    public static final byte PREFIX_DECREMENT = 42;
    public static final byte POSTFIX_DECREMENT = 43;

}
