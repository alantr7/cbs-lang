package com.github.alantr7.codebots.cbslang.high.parser.ast.expressions;

import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.Type;

public class Cast implements Operand {

    public Operand operand;

    public Type type;

    public Cast(Operand operand, Type type) {
        this.operand = operand;
        this.type = type;
    }

    @Override
    public Type getResultType() {
        return type;
    }

}
