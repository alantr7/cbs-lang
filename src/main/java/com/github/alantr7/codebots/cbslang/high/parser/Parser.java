package com.github.alantr7.codebots.cbslang.high.parser;

import com.github.alantr7.codebots.cbslang.high.parser.ast.AST;

public class Parser {

    private final TokenQueue tokens;

    public Parser(TokenQueue tokens) {
        this.tokens = tokens;
    }

    AST parse() {
        parseStatement();
        return null;
    }

    void parseStatement() {
        parseVariableDeclare();
    }

    void parseVariableDeclare() {
        String type = tokens.next();
        String identifier = tokens.next();
    }

    public static AST parse(String code) {
        return new Parser(Tokenizer.tokenize(code.split("\n"))).parse();
    }

}
