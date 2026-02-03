package com.github.alantr7.codebots.cbslang.high.parser.ast;

import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.Function;
import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.FunctionSignature;
import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.Variable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AST {

    public List<FunctionSignature> signatures = new LinkedList<>();

    public Map<Object, Integer> constants = new HashMap<>();

    public Map<String, Function> functions = new HashMap<>();

    public Map<String, Variable> globals = new HashMap<>();

    public int getFunctionOffset(FunctionSignature signature) {
        return signatures.indexOf(signature);
    }

    public int getConstantAddress(Object constant) {
        return constants.getOrDefault(constant, -1);
    }

}
