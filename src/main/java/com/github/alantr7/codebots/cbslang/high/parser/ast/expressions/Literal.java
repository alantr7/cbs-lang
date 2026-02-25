package com.github.alantr7.codebots.cbslang.high.parser.ast.expressions;

import com.github.alantr7.codebots.cbslang.high.parser.ast.statements.Statement;

public class Literal implements Statement, Operand {

    public static final byte INT = 0;
    public static final byte FLOAT = 1;

    public Number value;

    public byte type;

    public Literal() {
    }

    public Literal(byte type, Number value) {
        this.type = type;
        this.value = value;
    }

}
