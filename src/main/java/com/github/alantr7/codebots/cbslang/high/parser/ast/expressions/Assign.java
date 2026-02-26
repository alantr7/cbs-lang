package com.github.alantr7.codebots.cbslang.high.parser.ast.expressions;

import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.Type;
import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.Variable;
import com.github.alantr7.codebots.cbslang.high.parser.ast.statements.Statement;

public class Assign implements Operand, Statement {

    public Variable variable;

    public Operand[] indices;

    public Operand value;

    public static final byte SET = (byte) 30;

    public Assign(Variable variable, Operand[] indices, Operand value) {
        this.variable = variable;
        this.indices = indices;
        this.value = value;
    }

    @Override
    public Type getResultType() {
        return variable.type;
    }

}
