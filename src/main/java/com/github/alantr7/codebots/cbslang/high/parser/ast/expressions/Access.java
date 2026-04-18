package com.github.alantr7.codebots.cbslang.high.parser.ast.expressions;

import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.Type;
import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.Variable;
import com.github.alantr7.codebots.cbslang.high.parser.ast.statements.Statement;

public class Access implements Operand, Statement {

    public Variable variable;

    public Operand[] indices;

    public Access(Variable variable, Operand[] indices) {
        this.variable = variable;
        this.indices = indices;
    }

    @Override
    public Type getResultType() {
        return variable.type;
    }

}
