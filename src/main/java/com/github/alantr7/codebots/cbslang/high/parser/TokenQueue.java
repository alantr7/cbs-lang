package com.github.alantr7.codebots.cbslang.high.parser;

import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.Type;
import lombok.Getter;

public class TokenQueue {

    private final String[][] queue;

    private final Integer[] lines;

    @Getter
    private final Constant[] constants;

    private int row = 0;

    private int col = 0;

    public TokenQueue(String[][] queue, Integer[] lines, Constant[] constants) {
        this.queue = queue;
        this.lines = lines;
        this.constants = constants;

        if (queue[0].length == 0)
            advance();
    }

    public String peek() {
        if (isEmpty())
            return null;

        return queue[row][col];
    }

    public String next() {
        var token = queue[row][col];
        advance();

        return token;
    }

    public void rollback() {
        col--;

        if (col < 0) {
            row--;
            col = queue[row].length - 1;
        }
    }

    public void advance() {
        col++;

        if (col >= queue[row].length) {
            row++;
            col = 0;
        }

        if (!isEmpty() && queue[row].length == 0)
            advance();
    }

    public int getLine() {
        return lines[row];
    }

    public boolean isEmpty() {
        return row >= queue.length;
    }

    public static class Constant {
        public final Type type;
        public final Object value;

        public Constant(Type type, Object value) {
            this.type = type;
            this.value = value;
        }
    }

}
