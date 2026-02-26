package com.github.alantr7.codebots.cbslang.high.parser.ast.statements;

import com.github.alantr7.codebots.cbslang.high.parser.ast.expressions.Operand;

public class Ret implements Statement {

    public Operand value;

    public int cleanupCount;

    public Ret(Operand value, int cleanupCount) {
        this.value = value;
        this.cleanupCount = cleanupCount;
    }

}
