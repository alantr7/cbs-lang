package com.github.alantr7.codebots.cbslang.high.parser.ast.expressions;

public class Logical implements Operand {

    public Operand[] operands;

    public Logical() {
    }

    public Logical(Operand[] operands) {
        this.operands = operands;
    }

    public static final byte AND = 10;

    public static final byte OR = 11;

}
