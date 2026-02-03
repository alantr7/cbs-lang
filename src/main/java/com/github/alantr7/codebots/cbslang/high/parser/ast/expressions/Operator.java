package com.github.alantr7.codebots.cbslang.high.parser.ast.expressions;

import com.github.alantr7.codebots.cbslang.low.runtime.ProgramExecutor;

public class Operator implements Operand {

    public byte type;

    public static final Operator ADD = new Operator(ProgramExecutor.ADD);
    public static final Operator SUB = new Operator(ProgramExecutor.SUB);
    public static final Operator MUL = new Operator(ProgramExecutor.MUL);
    public static final Operator DIV = new Operator(ProgramExecutor.DIV);

    public static final Operator AND = new Operator(Logical.AND);
    public static final Operator OR = new Operator(Logical.OR);

    private Operator(byte type) {
        this.type = type;
    }

}
