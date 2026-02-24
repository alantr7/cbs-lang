package com.github.alantr7.codebots.cbslang.high.parser;

import com.github.alantr7.codebots.cbslang.exceptions.ParserException;
import com.github.alantr7.codebots.cbslang.high.parser.ast.AST;
import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.FunctionSignature;
import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.Primitive;
import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.Type;

public class Parser {

    private final TokenQueue tokens;

    private final AST ast = new AST();

    public Parser(TokenQueue tokens) {
        this.tokens = tokens;
    }

    AST parse() throws ParserException {
        while (!tokens.isEmpty()) {
            String nextToken = tokens.peek();
            System.out.println("Token: " + nextToken);

            // allowed in root context:
            // - imports
            // - structs
            // - global variables
            // - functions

            if (nextToken.equals("import")) {
                // parse import
                tokens.advance();
            }
            else if (nextToken.equals("struct")) {
                // parse struct
                tokens.advance();
            }
            else {
                // try to find out if it's a variable or a function
                parseFunctionOrVariable();
            }

        }
        return null;
    }

    void parseFunctionOrVariable() throws ParserException {
        String rawType = tokens.next();
        Type type = switch (rawType) {
            case "int" -> Primitive.INT;
            case "float" -> Primitive.FLOAT;
            case "string" -> Primitive.STRING;
            case "void" -> Primitive.VOID;
            default -> null;
        };

        // todo: check if it's variable or function anyway and then throw exception with more useful message
        if (type == null)
            throw new ParserException("Unexpected token '" + rawType + "'.");

        String name = tokens.next();
        String differentiator = tokens.peek();

        if (differentiator.equals("(")) {
            // it's a function
            parseFunction(type, name);
            return;
        }
        if (differentiator.equals("=") || differentiator.equals(";")) {
            parseVariable(type, name);
            return;
        }
    }

    void parseFunction(Type type, String name) throws ParserException {
        ParserHelper.expect(tokens.next(), "(");

        // todo: parse parameters

        ParserHelper.expect(tokens.next(), ")");
        ParserHelper.expect(tokens.next(), "{");

        // todo: parse function body

        ParserHelper.expect(tokens.next(), "}");

        FunctionSignature signature = new FunctionSignature(null, name, type, new Type[0]);
        ast.signatures.add(signature);

        System.out.println("Function parsed!");
    }

    void parseVariable(Type type, String name) {

    }

    public static AST parse(String code) throws ParserException {
        return new Parser(Tokenizer.tokenize(code.split("\n"))).parse();
    }

}
