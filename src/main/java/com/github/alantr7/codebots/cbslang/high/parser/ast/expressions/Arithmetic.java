package com.github.alantr7.codebots.cbslang.high.parser.ast.expressions;

public class Arithmetic implements Operand {

    public Operand[] operands;

    public Arithmetic() {
    }

    public Arithmetic(Operand[] operands) {
        this.operands = operands;
    }

}
