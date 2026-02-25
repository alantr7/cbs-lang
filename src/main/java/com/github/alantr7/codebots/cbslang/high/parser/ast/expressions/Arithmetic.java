package com.github.alantr7.codebots.cbslang.high.parser.ast.expressions;

import com.github.alantr7.codebots.cbslang.high.parser.ast.statements.Statement;

public class Arithmetic implements Statement, Operand {

    public Operand[] operands;

    public Arithmetic() {
    }

    public Arithmetic(Operand[] operands) {
        this.operands = operands;
    }

}
