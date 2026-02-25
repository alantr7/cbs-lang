package com.github.alantr7.codebots.cbslang.high.parser;

import com.github.alantr7.codebots.cbslang.exceptions.ParserException;
import com.github.alantr7.codebots.cbslang.high.parser.ast.AST;
import com.github.alantr7.codebots.cbslang.high.parser.ast.expressions.*;
import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.*;
import com.github.alantr7.codebots.cbslang.high.parser.ast.statements.Declare;
import com.github.alantr7.codebots.cbslang.high.parser.ast.statements.Ret;
import com.github.alantr7.codebots.cbslang.high.parser.ast.statements.Statement;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Stack;

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
        Type type = parseType(rawType);

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
        Scope functionScope = context.getCurrentScope().createChild(false);
        context.scopes.add(functionScope);

        Type[] parameterTypes = new Type[8];
        Variable[] parameterVariables = new Variable[8];
        int parameterCount = 0;
        for (; parameterCount < parameterTypes.length; parameterCount++) {
            String rawParameterType = tokens.peek();
            if (rawParameterType.equals(")"))
                break;

            tokens.advance();
            Type parameterType = parseType(rawParameterType);
            if (parameterType == null)
                throw new ParserException("Unexpected token '" + rawParameterType + "'.");

            String parameterName = tokens.next();
            Variable parameterVariable = new Variable(parameterType, false, 0, 1);
            functionScope.variables.put(parameterName, parameterVariable);
            functionScope.localVariables.put(parameterName, parameterVariable);

            parameterTypes[parameterCount] = parameterType;
            parameterVariables[parameterCount] = parameterVariable;

            if (tokens.peek().equals(",")) {
                tokens.advance();
                continue;
            }

            break;
        }

        // set parameter offsets
        // todo: test this
        for (int i = 0; i < parameterCount; i++) {
            parameterVariables[i].offset = i - parameterCount;
        }

        ParserHelper.expect(tokens.next(), ")");

        FunctionSignature signature = new FunctionSignature(null, name, type, Arrays.copyOf(parameterTypes, parameterCount));
        ast.signatures.add(signature);

        context.currentFunction = signature;

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

            ParserHelper.expect(tokens.next(), ";");
            body[statementCount] = statement;
        }

        ParserHelper.expect(tokens.next(), "}");

        Function function = new Function(signature, Arrays.copyOf(body, statementCount));
        ast.functions.put(name, function);

        System.out.println("Function parsed!");
        context.currentFunction = null;
    }

    Statement parseStatement() throws ParserException {
        String nextToken = tokens.peek();

        switch (nextToken) {
            case "return":
                return parseReturn();
            default:
                break;
        }

        // todo: ifs, else-ifs, loops, etc.
        tokens.advance();

        // variable declare
        Type parameterType = parseType(nextToken);
        if (parameterType != null) {
            return parseVariableDeclare(parameterType, tokens.next());
        }

        // variable assign
        if (tokens.peek().equals("=")) {
            tokens.advance();

            return parseVariableAssign(nextToken);
        }

        tokens.rollback();

        // todo: other types of expressions
        return (Arithmetic) parseExpression();
    }

    Declare parseVariableDeclare(Type type, String name) throws ParserException {
        Operand initialValue;
        // no assignment
        if (tokens.peek().equals(";")) {
            // todo: arrays
            initialValue = null;
        }
        else if (tokens.peek().equals("=")) {
            tokens.advance();
            initialValue = parseExpression();
        }
        else return null;

        if (context.getCurrentScope().localVariables.containsKey(name)) {
            throw new ParserException("Variable with name '" + name + "' already exists in this scope.");
        }

        Variable variable = new Variable(type, context.scopes.size() == 1, context.getCurrentScope().nextVariableOffset++, 1);
        context.getCurrentScope().variables.put(name, variable);
        context.getCurrentScope().localVariables.put(name, variable);
        return new Declare(type, initialValue, new int[] { 1 });
    }

    Assign parseVariableAssign(String name) throws ParserException {
        Operand value = parseExpression();
        Variable variable = context.getCurrentScope().variables.get(name);

        if (variable == null)
            throw new ParserException("Unknown variable '" + name + "'.");

        return new Assign(variable, new Operand[0], value);
    }

    Ret parseReturn() throws ParserException {
        tokens.advance();

        Operand value = parseExpression();
        return new Ret(value);
    }

    Operand parseExpression() throws ParserException {
        int j = 0;
        var stack = new Stack<String>();

        var postfix = new LinkedList<Operand>();

        stack.push("#");

        boolean expectsOperator = false;
        int parenthesisOpen = 0;

        while (!tokens.isEmpty()) {
            var next = tokens.peek();

            tokens.advance();

            if (expectsOperator && !ParserHelper.isOperator(next)) {
                tokens.rollback();
                break;
            }

            if (next.equals(")") && parenthesisOpen == 0) {
                tokens.rollback();
                break;
            }

            if (next.equals(";")) {
                tokens.rollback();
                break;
            }

            // TODO: Used !isOperator before, it must support parenthesis!
            if (ParserHelper.isNumber(next)) {
                postfix.add(new Literal(Literal.INT, Integer.parseInt(next)));
                j++;

                expectsOperator = true;
            }
            // todo: should i do null? probs not
//            else if (ParserHelper.isNull(next)) {
//                postfix.add(new LiteralExpression("null", LiteralExpression.NULL));
//                j++;
//
//                expectsOperator = true;
//            }
            else if (ParserHelper.isOperator(next)) {
                if (next.equals("(")) {
                    stack.push(next);
                    parenthesisOpen++;
//                }
                } else {
                    if (next.equals(")")) {
                        if (stack.isEmpty())
                            return null;

                        while (!stack.peek().equals("(")) {
                            var popInParenthesis = stack.pop();
                            // second argument was a string in old code. if this breaks that's the cause
                            Operand operator = parseOperator(popInParenthesis);
                            postfix.add(operator != null ? operator : new Literal(Literal.INT, Integer.parseInt(popInParenthesis)));
                            j++;
                        }

                        parenthesisOpen--;
                        stack.pop(); // pop out '('
                    } else {

                        if (ParserHelper.getPrecedence(next) > ParserHelper.getPrecedence(stack.peek())) {
                            stack.push(next);
                        } else {
                            while (ParserHelper.getPrecedence(next) <= ParserHelper.getPrecedence(stack.peek())) {
                                // todo: operator might be ( or ) but i highly doubt it
                                postfix.add(parseOperator(stack.pop()));
                                j++;
                            }

                            stack.push(next);
                        }

                        expectsOperator = false;
//                    }
//                }
                    }
                }
            } else {

                // Check if it's a record instantiation
//                if (next.equals("new")) {
//                    var recordInstantiate = nextRecordInstantiate();
//                    if (recordInstantiate == null)
//                        break;
//
//                    expectsOperator = true;
//                    postfix.add(recordInstantiate);
//                    continue;
//                }
//
                 // can not mix strings with numbers here!
//                if (next.startsWith("\"") && next.endsWith("\"")) {
//                    postfix.add(new Literal(next.substring(1, next.length() - 1), LiteralExpression.STRING));
//                } else {
                    tokens.rollback();

                    // todo: function calls or array access
                    var memberAccess = parseVariableAccessOrCall();
                    if (memberAccess == null) {
                        break;
                    } else {
                        postfix.add(memberAccess);
                    }
//                }

                expectsOperator = true;

            }

        }

        while (!stack.peek().equals("#")) {
            var pop = stack.pop();
            postfix.add(parseOperator(pop));
            j++;
        }

        for (int i = 0; i < postfix.size(); i++) {
            Operand operand = postfix.get(i);
            if (operand instanceof Operator operator) {
                // consume two literals
                Operand prev2 = postfix.remove(i - 1);
                i--;

                Operand prev1 = postfix.remove(i - 1);
                i--;

                postfix.remove(i);
                postfix.add(i, new Arithmetic(new Operand[]{
                  prev1, prev2, operator
                }));
            }
        }

        return postfix.getFirst();
    }

    Operand parseVariableAccessOrCall() throws ParserException {
        String variableName = tokens.next();

        if (tokens.peek().equals("(")) {
            tokens.advance();
            ParserHelper.expect(tokens.next(), ")");

            Function function = ast.functions.get(variableName);
            if (function != null)
                return new Call(function.signature, new Operand[0][0]);
        }
        else {
            Variable variable = context.getCurrentScope().variables.get(variableName);
            if (variable != null)
                return new Access(variable, new Operand[0]);
        }

        throw new ParserException("Unknown member '" + variableName + "'.");
    }

    Operand parseOperator(String raw) {
        return switch (raw) {
            case "+" -> Operator.ADD;
            case "-" -> Operator.SUB;
            case "*" -> Operator.MUL;
            case "/" -> Operator.DIV;
            default -> null;
        };
    }

    Type parseType(String token) {
        return switch (token) {
            case "int" -> Primitive.INT;
            case "float" -> Primitive.FLOAT;
            case "string" -> Primitive.STRING;
            case "void" -> Primitive.VOID;
            default -> null;
        };
    }

    public static AST parse(String code) throws ParserException {
        return new Parser(Tokenizer.tokenize(code.split("\n"))).parse();
    }

}
