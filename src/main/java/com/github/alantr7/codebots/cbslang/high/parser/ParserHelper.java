package com.github.alantr7.codebots.cbslang.high.parser;

import com.github.alantr7.codebots.cbslang.exceptions.ParserException;

import java.util.Set;

public class ParserHelper {

    private static final Set<String> OPERATORS = Set.of(
      "(", ")",
      "*", "/",
      "+", "-",
      ">", "<", ">=", "<=",
      "==", "!=",
      "&",
      "^",
      "|",
      "&&",
      "||",
      "="
    );
    public static boolean isOperator(String input) {
        if (input.length() > 2 || input.isEmpty())
            return false;

        return OPERATORS.contains(input);
    }

    private static final Set<String> UNARY = Set.of(
      "++",
      "--"
    );
    public static boolean isUnaryOperator(String input) {
        return UNARY.contains(input);
    }

    public static boolean isNumber(String input) {
        return input.matches("\\d+");
    }

    public static boolean isBoolean(String input) {
        return input.equals("true") || input.equals("false");
    }

    public static boolean isNull(String input) {
        return input.equals("null");
    }

    public static int getPrecedence(String input) {
        return switch (input) {
            case "(", ")", "#" -> 1; // was 1
            case "="  -> 2; // maybe above NEEDS to be 1. check if breaks
            case "||" -> 3;
            case "&&" -> 4;
            case "<", ">", "==", "!=", "<=", ">=" -> 7;
            case "+", "-" -> 8;
            case "*", "/" -> 9;
            default -> 0;
        };
    }

    public static void expect(String token, String expected) throws ParserException {
        if (!token.equals(expected)) {
            throw new ParserException("Unexpected token: \"" + token + "\". Was expecting \"" + expected + "\".");
        }
    }

    public static void error(String message) throws ParserException {
        throw new ParserException(message);
    }

}
