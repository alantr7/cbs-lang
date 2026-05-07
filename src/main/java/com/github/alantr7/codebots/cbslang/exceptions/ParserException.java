package com.github.alantr7.codebots.cbslang.exceptions;

import lombok.Getter;
import lombok.Setter;

public class ParserException extends Exception {

    @Getter @Setter
    protected int line;

    public ParserException(String message) {
        super(message);
    }

}
