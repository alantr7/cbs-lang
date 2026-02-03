package com.github.alantr7.codebots.cbslang.high.parser.ast.statements;

import com.github.alantr7.codebots.cbslang.high.parser.ast.expressions.Operand;
import org.jetbrains.annotations.Nullable;

public class For implements Statement {

    @Nullable
    public ForInitExpr init;

    @Nullable
    public Operand condition;

    @Nullable
    public Operand update;

    public Statement[] body;

    public For(@Nullable ForInitExpr init, @Nullable Operand condition, @Nullable Operand update, Statement[] body) {
        this.init = init;
        this.condition = condition;
        this.update = update;
        this.body = body;
    }

}
