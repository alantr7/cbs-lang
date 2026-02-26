package com.github.alantr7.codebots.cbslang.high.parser.ast.expressions;

import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.Primitive;
import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.Type;
import com.github.alantr7.codebots.cbslang.high.parser.ast.statements.Statement;

public class Arithmetic implements Statement, Operand {

    public Operand[] operands;

    public Arithmetic() {
    }

    public Arithmetic(Operand[] operands) {
        this.operands = operands;
    }

    @Override
    public Type getResultType() {
        // todo: check if one of operands is a float
        return Primitive.INT;
    }

}
