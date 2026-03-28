package com.github.alantr7.codebots.cbslang.high.parser;

import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.FunctionSignature;

import java.util.Stack;

public class ParserContext {

    Stack<Scope> scopes = new Stack<>();

    Stack<Scope> loopScopes = new Stack<>();

    FunctionSignature currentFunction;

    public Scope getCurrentScope() {
        return scopes.peek();
    }

    public Scope nestScope(boolean copyLocals, boolean copyVariableOffset) {
        Scope scope = getCurrentScope().createChild(copyLocals, copyVariableOffset);
        scopes.add(scope);

        return scope;
    }

}
