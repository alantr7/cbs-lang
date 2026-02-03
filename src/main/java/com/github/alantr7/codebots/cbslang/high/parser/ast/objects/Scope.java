package com.github.alantr7.codebots.cbslang.high.parser.ast.objects;

import java.util.HashMap;
import java.util.Map;

public class Scope {

    public Map<String, Variable> variables = new HashMap<>();

    public Scope nest() {
        Scope scope = new Scope();
        scope.variables = new HashMap<>(variables);

        return scope;
    }

}
