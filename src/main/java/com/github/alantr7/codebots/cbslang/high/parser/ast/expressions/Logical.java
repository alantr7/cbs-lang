package com.github.alantr7.codebots.cbslang.high.parser.ast.expressions;

import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.Primitive;
import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.Type;

public class Logical implements Operand {

    public Operand[] operands;

    public Logical() {
    }

    public Logical(Operand[] operands) {
        this.operands = operands;
    }

    public static final byte AND = 10;

    public static final byte OR = 11;

    @Override
    public Type getResultType() {
        return Primitive.INT;
    }

}
