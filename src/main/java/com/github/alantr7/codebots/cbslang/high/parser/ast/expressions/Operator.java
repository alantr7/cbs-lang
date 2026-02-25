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

    public static final Operator EQUALS = new Operator(Compare.EQUALS);
    public static final Operator NOT_EQUALS = new Operator(Compare.NOT_EQUALS);
    public static final Operator LESS_THAN = new Operator(Compare.LESS_THAN);
    public static final Operator GREATER_THAN = new Operator(Compare.GREATER_THAN);
    public static final Operator LESS_EQUALS = new Operator(Compare.LESS_EQUALS);
    public static final Operator GREATER_EQUALS = new Operator(Compare.GREATER_EQUALS);

    private Operator(byte type) {
        this.type = type;
    }

}
