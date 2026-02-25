package com.github.alantr7.codebots.cbslang.high.parser;

import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.Function;
import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.FunctionSignature;

import java.util.Stack;

public class ParserContext {

    Stack<Scope> scopes = new Stack<>();

    FunctionSignature currentFunction;

    public Scope getCurrentScope() {
        return scopes.peek();
    }

}
