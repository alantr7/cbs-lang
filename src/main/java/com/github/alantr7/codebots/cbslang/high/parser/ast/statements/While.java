package com.github.alantr7.codebots.cbslang.high.parser.ast.statements;

import com.github.alantr7.codebots.cbslang.high.parser.ast.expressions.Operand;

public class While extends If {

    public boolean isDoWhile;

    public While(Operand expression, Statement[] body) {
        this(expression, body, false);
    }

    public While(Operand expression, Statement[] body, boolean isDoWhile) {
        super(expression, body, null);
        this.isDoWhile = isDoWhile;
    }

}
