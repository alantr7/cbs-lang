package com.github.alantr7.codebots.cbslang.high.parser.ast.statements;

import com.github.alantr7.codebots.cbslang.high.parser.ast.expressions.Operand;
import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.Type;
import org.jetbrains.annotations.Nullable;

public class Declare implements Statement, ForInitExpr {

    public Type type;

    @Nullable
    public Operand value;

    public int[] lengths;

    public Declare(Type type, @Nullable Operand value, int[] lengths) {
        this.type = type;
        this.value = value;
        this.lengths = lengths;
    }

}
