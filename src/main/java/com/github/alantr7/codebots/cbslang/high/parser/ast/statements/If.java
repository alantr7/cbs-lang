package com.github.alantr7.codebots.cbslang.high.parser.ast.statements;

import com.github.alantr7.codebots.cbslang.high.parser.Scope;
import com.github.alantr7.codebots.cbslang.high.parser.ast.expressions.Operand;
import org.jetbrains.annotations.Nullable;

public class If implements Statement {

    @Nullable
    public Operand expression;

    public Statement[] body;

    @Nullable
    public If elseStmt;

    public Scope scope;

    public If(@Nullable Operand expression, Statement[] body, Scope scope, @Nullable If elseStmt) {
        this.expression = expression;
        this.body = body;
        this.elseStmt = elseStmt;
        this.scope = scope;
    }

}
