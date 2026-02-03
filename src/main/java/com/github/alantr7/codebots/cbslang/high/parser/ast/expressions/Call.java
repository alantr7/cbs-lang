package com.github.alantr7.codebots.cbslang.high.parser.ast.expressions;

import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.FunctionSignature;
import com.github.alantr7.codebots.cbslang.high.parser.ast.statements.Statement;

public class Call implements Operand, Statement {

    public FunctionSignature function;

    public Operand[][] arguments;

    public Call(FunctionSignature function, Operand[][] arguments) {
        this.function = function;
        this.arguments = arguments;
    }

}
