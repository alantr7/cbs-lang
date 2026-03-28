package com.github.alantr7.codebots.cbslang.high.parser.ast.statements;

public class LoopCmd implements Statement {

    public static final byte CONTINUE = 0;

    public static final byte BREAK = 1;

    public final byte type;

    public final int variableCount;

    public LoopCmd(byte type, int variableCount) {
        this.type = type;
        this.variableCount = variableCount;
    }

}
