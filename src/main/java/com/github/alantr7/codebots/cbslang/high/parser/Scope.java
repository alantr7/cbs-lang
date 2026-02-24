package com.github.alantr7.codebots.cbslang.high.parser;

import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.Variable;

import java.util.HashMap;
import java.util.Map;

public class Scope {

    Map<String, Variable> variables = new HashMap<>();

    public Scope createChild() {
        Scope child = new Scope();
        child.variables.putAll(variables);

        return child;
    }

}
