package com.github.alantr7.codebots.cbslang.high.parser.ast.expressions;

import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.Variable;

public class Access implements Operand {

    public Variable variable;

    public Operand[] indices;

    public Access(Variable variable, Operand[] indices) {
        this.variable = variable;
        this.indices = indices;
    }

}
