package com.github.alantr7.codebots.cbslang.high.parser.ast.statements;

import com.github.alantr7.codebots.cbslang.high.parser.ast.expressions.Operand;
import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.Type;
import org.jetbrains.annotations.Nullable;

public class Declare implements Statement, ForInitExpr {

    public Type type;

    @Nullable
    public Operand value;

    public int length;

    public Declare(Type type, @Nullable Operand value, int length) {
        this.type = type;
        this.value = value;
        this.length = length;
    }

}
