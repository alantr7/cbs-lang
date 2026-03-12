package com.github.alantr7.codebots.cbslang.high.parser.ast.statements;

import com.github.alantr7.codebots.cbslang.high.parser.Scope;
import com.github.alantr7.codebots.cbslang.high.parser.ast.expressions.Operand;

public class While extends If {

    public boolean isDoWhile;

    public While(Operand expression, Statement[] body, Scope scope) {
        this(expression, body, scope, false);
    }

    public While(Operand expression, Statement[] body, Scope scope, boolean isDoWhile) {
        super(expression, body, scope, null);
        this.isDoWhile = isDoWhile;
    }

}
