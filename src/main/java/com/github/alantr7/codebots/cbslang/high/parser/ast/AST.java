package com.github.alantr7.codebots.cbslang.high.parser.ast;

import com.github.alantr7.codebots.cbslang.high.parser.TokenQueue;
import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.Function;
import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.FunctionSignature;
import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.Variable;

import java.util.*;

public class AST {

    public List<FunctionSignature> signatures = new LinkedList<>();

    public List<TokenQueue.Constant> constants = new LinkedList<>();

    public Map<String, Function> functions = new HashMap<>();

    public Map<String, Variable> globals = new HashMap<>();

    public int getFunctionOffset(FunctionSignature signature) {
        return signatures.indexOf(signature);
    }

    public int getConstantAddress(Object constant) {
        for (int i = 0; i < constants.size(); i++) {
            if (Objects.equals(constant, constants.get(i).value))
                return i;
        }
        return -1;
    }

}
