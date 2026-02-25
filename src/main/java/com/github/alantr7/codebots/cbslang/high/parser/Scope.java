package com.github.alantr7.codebots.cbslang.high.parser;

import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.Variable;

import java.util.HashMap;
import java.util.Map;

public class Scope {

    Map<String, Variable> variables = new HashMap<>();

    Map<String, Variable> localVariables = new HashMap<>();

    int nextVariableOffset;

    public Scope createChild(boolean copyLocals) {
        Scope child = new Scope();
        child.variables.putAll(variables);

        if (copyLocals)
            child.localVariables.putAll(localVariables);

        return child;
    }

}
