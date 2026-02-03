package com.github.alantr7.codebots.cbslang.high.parser.ast.objects;

public class Variable {

    public Type type;

    public boolean global;

    public int offset;

    public int[] lengths;

    public int length;

    public Variable(Type type, boolean global, int offset, int length) {
        this.type = type;
        this.global = global;
        this.offset = offset;
        this.lengths = new int[] { length };
        this.length = 1;
    }

    public Variable(Type type, boolean global, int offset, int[] length) {
        this.type = type;
        this.global = global;
        this.offset = offset;
        this.lengths = length;
        this.length = 1;
        for (int l : length)
            this.length *= l;
    }

}
