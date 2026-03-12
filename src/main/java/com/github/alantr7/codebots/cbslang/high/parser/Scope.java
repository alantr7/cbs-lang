package com.github.alantr7.codebots.cbslang.high.parser;

import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.Variable;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class Scope {

    @Getter
    Map<String, Variable> variables = new HashMap<>();

    @Getter
    Map<String, Variable> parameterVariables = new HashMap<>();

    @Getter
    Map<String, Variable> localVariables = new HashMap<>();

    int nextVariableOffset = 1;

    public Scope createChild(boolean copyLocals, boolean copyVariableOffset) {
        Scope child = new Scope();
        child.variables.putAll(variables);

        if (copyLocals)
            child.localVariables.putAll(localVariables);

        if (copyVariableOffset)
            child.nextVariableOffset = nextVariableOffset;

        return child;
    }

}
