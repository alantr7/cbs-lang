package com.github.alantr7.codebots.cbslang.high.parser.ast.objects;

import com.github.alantr7.codebots.cbslang.high.parser.Scope;
import com.github.alantr7.codebots.cbslang.high.parser.ast.statements.Statement;

public class Function {

    public FunctionSignature signature;

    public Statement[] body;

    public Scope scope;

    public Function(FunctionSignature signature, Statement[] body, Scope scope) {
        this.signature = signature;
        this.body = body;
        this.scope = scope;
    }

}
