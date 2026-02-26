package com.github.alantr7.codebots.cbslang.high.parser.ast.statements;

import com.github.alantr7.codebots.cbslang.high.parser.ast.expressions.Operand;

public class Ret implements Statement {

    public Operand value;

    public Ret(Operand value) {
        this.value = value;
    }

}
