package com.github.alantr7.codebots.cbslang.high.parser;

import com.github.alantr7.codebots.cbslang.exceptions.ParserException;
import com.github.alantr7.codebots.cbslang.high.parser.ast.AST;
import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.*;
import com.github.alantr7.codebots.cbslang.high.parser.ast.statements.Declare;
import com.github.alantr7.codebots.cbslang.high.parser.ast.statements.Statement;

import java.util.Arrays;

public class Parser {

    private final TokenQueue tokens;

    private final AST ast = new AST();

    private final ParserContext context = new ParserContext();

    public Parser(TokenQueue tokens) {
        this.tokens = tokens;
        this.context.scopes.push(new Scope());
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
        return ast;
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
            parseVariableDeclare(type, name);
            return;
        }
    }

    void parseFunction(Type type, String name) throws ParserException {
        ParserHelper.expect(tokens.next(), "(");
        Scope functionScope = context.getCurrentScope().createChild();

        int variableOffset = 0;

        Type[] parameterTypes = new Type[8];
        int parameterCount = 0;
        for (; parameterCount < parameterTypes.length; parameterCount++) {
            String rawParameterType = tokens.peek();
            if (rawParameterType.equals(")"))
                break;

            tokens.advance();
            Type parameterType = switch (rawParameterType) {
                case "int" -> Primitive.INT;
                case "float" -> Primitive.FLOAT;
                case "string" -> Primitive.STRING;
                default -> null;
            };
            if (parameterType == null)
                throw new ParserException("Unexpected token '" + rawParameterType + "'.");

            String parameterName = tokens.next();

            parameterTypes[parameterCount] = parameterType;
            functionScope.variables.put(parameterName, new Variable(parameterType, false, variableOffset++, 1));

            if (tokens.peek().equals(",")) {
                tokens.advance();
                continue;
            }

            break;
        }

        ParserHelper.expect(tokens.next(), ")");
        ParserHelper.expect(tokens.next(), "{");

        // todo: parse function body
        Statement[] body = new Statement[128];
        int statementCount = 0;
        for (; statementCount < body.length; statementCount++) {
            if (tokens.peek().equals("}"))
                break;

            Statement statement = parseStatement();
            if (statement == null)
                break;

            body[statementCount] = statement;
        }

        ParserHelper.expect(tokens.next(), "}");

        FunctionSignature signature = new FunctionSignature(null, name, type, Arrays.copyOf(parameterTypes, parameterCount));
        ast.signatures.add(signature);

        Function function = new Function(signature, Arrays.copyOf(body, statementCount));
        ast.functions.put(name, function);

        System.out.println("Function parsed!");
    }

    Statement parseStatement() throws ParserException {
        String nextToken = tokens.peek();

        // todo: ifs, else-ifs, loops, etc.
        tokens.advance();
        Type parameterType = switch (nextToken) {
            case "int" -> Primitive.INT;
            case "float" -> Primitive.FLOAT;
            case "string" -> Primitive.STRING;
            default -> null;
        };

        if (parameterType == null)
            throw new ParserException("Unexpected token '" + nextToken + "'.");

        return parseVariableDeclare(parameterType, tokens.next());
    }

    Declare parseVariableDeclare(Type type, String name) {
        if (tokens.peek().equals(";")) {
            tokens.advance();
            // no assignment
            // todo: arrays
            return new Declare(type, null, new int[] { 1 });
        }

        // todo: assignment
        return null;
    }

    public static AST parse(String code) throws ParserException {
        return new Parser(Tokenizer.tokenize(code.split("\n"))).parse();
    }

}
