package com.github.alantr7.codebots.cbslang.high.parser.ast.expressions;

import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.Primitive;
import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.Type;
import com.github.alantr7.codebots.cbslang.high.parser.ast.statements.Statement;

public class Concat implements Statement, Operand {

    public Operand left;

    public Operand right;

    public Concat(Operand left, Operand right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public Type getResultType() {
        return Primitive.STRING;
    }

}
