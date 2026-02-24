package com.github.alantr7.codebots.cbslang.high.parser;

import java.util.Stack;

public class ParserContext {

    Stack<Scope> scopes = new Stack<>();

    public Scope getCurrentScope() {
        return scopes.peek();
    }

}
