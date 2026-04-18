package com.github.alantr7.codebots.cbslang.high.parser;

import com.github.alantr7.codebots.cbslang.exceptions.ParserException;
import com.github.alantr7.codebots.cbslang.high.parser.ast.AST;
import com.github.alantr7.codebots.cbslang.high.parser.ast.expressions.*;
import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.*;
import com.github.alantr7.codebots.cbslang.high.parser.ast.statements.*;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.ExternalFunction;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.Module;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.ModuleRepository;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Stack;

import static com.github.alantr7.codebots.cbslang.high.parser.ParserHelper.expect;

public class Parser {

    private final TokenQueue tokens;

    private final ModuleRepository moduleRepository;

    private final AST ast = new AST();

    private final ParserContext context = new ParserContext();

    public Parser(TokenQueue tokens) {
        this(ModuleRepository.EMPTY, tokens);
    }

    public Parser(ModuleRepository modules, TokenQueue tokens) {
        this.moduleRepository = modules;
        this.tokens = tokens;
        this.context.scopes.push(new Scope());
        ast.constants.addAll(Arrays.asList(tokens.getConstants()));
    }

    AST parse() throws ParserException {
        Primitive.fix();

        // import "auto-import" modules
        for (Module module : moduleRepository.getModules()) {
            if (module.isAutoImport()) {
                ast.signatures.addAll(module.getFunctions().stream().map(ExternalFunction::createSignature).toList());
            }
        }

        while (!tokens.isEmpty()) {
            String nextToken = tokens.peek();

            // allowed in root context:
            // - imports
            // - structs
            // - global variables
            // - functions

            if (nextToken.equals("import")) {
                parseImport();
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

    void parseImport() throws ParserException {
        tokens.advance();
        String name = tokens.next();
        expect(tokens.next(), ";");

        Module module = moduleRepository.getModule(name);
        if (module == null)
            throw new ParserException("Unknown module '" + name + "'.");

        for (ExternalFunction fun : module.getFunctions()) {
            ast.signatures.add(fun.createSignature());
        }
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
        if (differentiator.equals("=") || differentiator.equals(";") || differentiator.equals("[")) {
            parseVariableDeclare(type, name, false);
            return;
        }
    }

    void parseFunction(Type type, String name) throws ParserException {
        expect(tokens.next(), "(");
        Scope functionScope = context.nestScope(false, false);

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
            functionScope.parameterVariables.put(parameterName, parameterVariable);

            parameterTypes[parameterCount] = parameterType;
            parameterVariables[parameterCount] = parameterVariable;

            if (tokens.peek().equals(",")) {
                tokens.advance();
                continue;
            }

            parameterCount++;
            break;
        }

        // set parameter offsets
        for (int i = 0; i < parameterCount; i++) {
            parameterVariables[i].offset = i - parameterCount - 1;
        }

        expect(tokens.next(), ")");

        FunctionSignature signature = new FunctionSignature(null, name, type, Arrays.copyOf(parameterTypes, parameterCount));
        ast.signatures.add(signature);

        context.currentFunction = signature;

        expect(tokens.next(), "{");

        // todo: parse function body
        Statement[] body = parseBody();

        expect(tokens.next(), "}");

        Function function = new Function(signature, body, functionScope);
        ast.functions.put(name, function);

        context.currentFunction = null;
        context.scopes.pop();
    }

    Statement[] parseBody() throws ParserException {
        Statement[] body = new Statement[128];
        int statementCount = 0;
        for (; statementCount < body.length; statementCount++) {
            if (tokens.peek().equals("}"))
                break;

            if (tokens.peek().equals(";")) {
                tokens.advance();
                continue;
            }

            Statement statement = parseStatement();
            if (statement == null)
                break;

            if ((!(statement instanceof If) && !(statement instanceof For)) || (statement instanceof While wh && wh.isDoWhile))
                expect(tokens.next(), ";");

            body[statementCount] = statement;
        }

        return Arrays.copyOf(body, statementCount);
    }

    Statement parseStatement() throws ParserException {
        return parseStatement(false);
    }

    Statement parseStatement(boolean forInitExpr) throws ParserException {
        String nextToken = tokens.peek();

        if (!forInitExpr) {
            switch (nextToken) {
                case "if":
                    return parseIf();
                case "while":
                    return parseWhile();
                case "do":
                    return parseDoWhile();
                case "for":
                    return parseFor();
                case "continue":
                case "break":
                    return parseLoopCommand();
                case "return":
                    return parseReturn();
                default:
                    break;
            }
        }

        long rollbackPos = tokens.createRollbackPosition();
        tokens.advance();

        // variable declare
        Type parameterType = parseType(nextToken);
        if (parameterType != null) {
            return parseVariableDeclare(parameterType, tokens.next(), forInitExpr);
        }

        // array access
        Operand[] access = new Operand[8];
        byte dimensionCount = 0;
        if (tokens.peek().equals("[")) {
            while (tokens.peek().equals("[")) {
                tokens.advance();
                Operand expression = parseExpression();
                if (expression == null || expression.getResultType() != Primitive.INT) {
                    throw new ParserException("Not an integer!");
                }

                access[dimensionCount++] = expression;
                expect(tokens.next(), "]");
            }
        }

        // variable assign
        if (tokens.peek().equals("=")) {
            tokens.advance();

            Operand[] accessTrimmed = new Operand[dimensionCount];
            System.arraycopy(access, 0, accessTrimmed, 0, dimensionCount);
            return parseVariableAssign(accessTrimmed, nextToken);
        }

        tokens.rollback(rollbackPos);

        Operand expression = parseExpression();
        if (expression instanceof Statement stmt)
            return stmt;

        throw new ParserException("Can not use " + expression + " as a statement.");
    }

    Declare parseVariableDeclare(Type type, String name, boolean isForInit) throws ParserException {
        Operand initialValue;
        int length = 1;
        int[] dimensions = new int[8];
        byte dimensionCount = 0;

        // no assignment
        if (tokens.peek().equals(";")) {
            initialValue = null;
            dimensions[0] = length;
            dimensionCount = 1;
        }
        // array
        else if (tokens.peek().equals("[")) {
            while (tokens.peek().equals("[")) {
                // todo: arrays
                tokens.advance();
                Operand lengthLiteral = parseExpression();

                if (!(lengthLiteral instanceof Literal literal) || literal.type != Literal.INT || (int) literal.value < 1) {
                    throw new ParserException("Array dimension length must be a positive integer literal.");
                }

                length *= (int) literal.value;
                dimensions[dimensionCount++] = (int) literal.value;
                expect(tokens.next(), "]");
            }
            initialValue = null;
        }
        // non-array assignment
        else if (tokens.peek().equals("=")) {
            tokens.advance();
            initialValue = parseExpression();

            if (type != initialValue.getResultType()) {
                if (type == Primitive.FLOAT && initialValue.getResultType() == Primitive.INT)
                    initialValue = new Cast(initialValue, Primitive.FLOAT);
                else
                    throw new ParserException("Type mismatch: can not convert '" + initialValue.getResultType() + "' to '" + type + "'.");
            }

            dimensions[0] = length;
            dimensionCount = 1;
        }
        else return null;

        if (context.getCurrentScope().localVariables.containsKey(name)) {
            throw new ParserException("Variable with name '" + name + "' already exists in this scope.");
        }

        int[] lengths = new int[dimensionCount];
        System.arraycopy(dimensions, 0, lengths, 0, dimensionCount);

        Variable variable = new Variable(type, context.scopes.size() == 1, context.getCurrentScope().nextVariableOffset, lengths);
        context.getCurrentScope().nextVariableOffset += length;
        context.getCurrentScope().variables.put(name, variable);
        if (!isForInit) {
            context.getCurrentScope().localVariables.put(name, variable);
        }
        return new Declare(type, initialValue, length);
    }

    Assign parseVariableAssign(Operand[] access, String name) throws ParserException {
        Operand value = parseExpression();
        Variable variable = context.getCurrentScope().variables.get(name);

        if (variable == null)
            throw new ParserException("Unknown variable '" + name + "'.");

        if (variable.type != value.getResultType())
            throw new ParserException("Type mismatch: can not convert '" + value.getResultType() + "' to '" + variable.type + "'.");

        return new Assign(variable, access, value);
    }

    If parseIf() throws ParserException {
        tokens.advance();
        expect(tokens.next(), "(");

        Operand condition = parseExpression();

        expect(tokens.next(), ")");
        expect(tokens.next(), "{");

        Scope scope = context.nestScope(false, true);
        Statement[] body = parseBody();
        context.scopes.pop();

        expect(tokens.next(), "}");

        if (!tokens.peek().equals("else")) {
            return new If(condition, body, scope, null);
        }

        tokens.advance();
        if (tokens.peek().equals("if")) {
            return new If(condition, body, scope, parseIf());
        } else {
            expect(tokens.next(), "{");

            Scope elseScope = context.nestScope(false, true);
            Statement[] elseBody = parseBody();
            context.scopes.pop();

            expect(tokens.next(), "}");
            return new If(condition, body, scope, new If(null, elseBody, elseScope, null));
        }
    }

    While parseWhile() throws ParserException {
        tokens.advance();
        expect(tokens.next(), "(");

        Operand condition = parseExpression();

        expect(tokens.next(), ")");
        expect(tokens.next(), "{");
        Scope scope = context.nestScope(false, true);
        context.loopScopes.push(scope);
        Statement[] body = parseBody();
        context.scopes.pop();
        context.loopScopes.pop();
        expect(tokens.next(), "}");

        return new While(condition, body, scope);
    }

    While parseDoWhile() throws ParserException {
        tokens.advance();
        expect(tokens.next(), "{");
        Scope scope = context.nestScope(false, true);
        context.loopScopes.push(scope);
        Statement[] body = parseBody();
        context.scopes.pop();
        context.loopScopes.pop();
        expect(tokens.next(), "}");
        expect(tokens.next(), "while");
        expect(tokens.next(), "(");
        Operand condition = parseExpression();
        expect(tokens.next(), ")");

        return new While(condition, body, scope, true);
    }

    For parseFor() throws ParserException {
        tokens.advance();
        expect(tokens.next(), "(");

        // todo: parse ForInitExpr
        Statement init = tokens.peek().equals(";") ? null : parseStatement(true);

        expect(tokens.next(), ";");

        Operand condition = parseExpression();
        expect(tokens.next(), ";");

        Operand update = parseExpression();

        expect(tokens.next(), ")");
        expect(tokens.next(), "{");
        Scope scope = context.nestScope(false, true);
        context.loopScopes.push(scope);
        Statement[] body = parseBody();
        expect(tokens.next(), "}");
        context.scopes.pop();
        context.loopScopes.pop();

        return new For((ForInitExpr) init, condition, update, body, scope);
    }

    LoopCmd parseLoopCommand() throws ParserException {
        String cmd = tokens.next();

        // count local variables
        int variableCount = 0;
        int index = context.scopes.indexOf(context.loopScopes.peek());
        for (int i = index; i < context.scopes.size(); i++) {
            variableCount += context.scopes.get(i).getMemoryUse();
        }

        if (cmd.equals("continue")) {
            return new LoopCmd(LoopCmd.CONTINUE, variableCount);
        }
        return new LoopCmd(LoopCmd.BREAK, variableCount);
    }

    Ret parseReturn() throws ParserException {
        tokens.advance();

        Operand value = parseExpression();
        if (value.getResultType() != context.currentFunction.returnType)
            throw new ParserException("Type mismatch: can not convert '" + value.getResultType() + "' to '" + context.currentFunction.returnType + "'.");

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
            var next = tokens.next();

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

            else if (ParserHelper.isCastOperator(next)) {
                postfix.add(new Cast(parseExpression(), next.equals("(int)") ? Primitive.INT : Primitive.FLOAT));
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

                // todo: check if operation can be performed on these two operands

                postfix.remove(i);
                if (operator.type >= (byte) 30) {
                    if (!(prev1 instanceof Access access))
                        throw new ParserException("Can not assign to non variable.");

                    postfix.add(i, new Assign(access.variable, new Operand[0], prev2));
                }
                else if (operator.type >= (byte) 20) {
                    postfix.add(i, new Compare(prev1, operator.type, prev2));
                }
                else if (operator.type >= (byte) 10) {
                    postfix.add(i, new Logical(new Operand[]{
                      prev1, operator, prev2
                    }));
                } else {
                    if (prev1.getResultType() == Primitive.STRING || prev2.getResultType() == Primitive.STRING) {
                        if (operator.type != Operator.ADD.type)
                            throw new ParserException("Invalid operation on string.");

                        postfix.add(i, new Concat(prev1, prev2));
                    } else {
                        postfix.add(i, new Arithmetic(new Operand[]{prev1, prev2, operator}));
                    }
                }
            }
        }

        return postfix.isEmpty() ? null : postfix.getFirst();
    }

    Operand parseVariableAccessOrCall() throws ParserException {
        // check if it's access to constant
        if (tokens.peek().charAt(0) == '@') {
            // todo: load type from constants, this is just testing
            int index = Integer.parseInt(tokens.next().substring(1));
            TokenQueue.Constant constant = tokens.getConstants()[index];
            return new Access(new Variable(constant.type, true, index, 1), new Operand[0]);
        }

        byte prefix = 0;
        byte postfix = 0;

        if (tokens.peek().equals("++")) {
            tokens.advance();
            prefix = Unary.PREFIX_INCREMENT;
        }
        else if (tokens.peek().equals("--")) {
            tokens.advance();
            prefix = Unary.PREFIX_DECREMENT;
        }

        String nextToken = tokens.next();
        if ((prefix == 0) && (tokens.peek().equals("(") || tokens.peek().equals("."))) {
            String moduleName;
            String functionName;
            if (tokens.peek().equals(".")) {
                tokens.advance();
                moduleName = nextToken;
                functionName = tokens.next();
                expect(tokens.peek(), "(");
            } else {
                moduleName = null;
                functionName = nextToken;
            }
            tokens.advance();

            FunctionSignature function = ast.signatures.stream().filter(s -> {
                if (s.name.equals(functionName) && Objects.equals(moduleName, s.module))
                    return true;

                if (s.name.equals(functionName) && moduleRepository.getModule(s.module) != null && moduleRepository.getModule(s.module).isAutoImport())
                    return true;

                return false;
            }).findFirst().orElse(null);
            if (function == null)
                throw new ParserException("Unknown member '" + functionName + "'.");

            Operand[][] arguments = new Operand[8][1];
            int argumentCount = 0;
            for (; argumentCount < arguments.length; argumentCount++) {
                if (tokens.peek().equals(")")) {
                    tokens.advance();
                    break;
                }

                Operand argument = parseExpression();
                arguments[argumentCount][0] = argument;

                if (tokens.peek().equals(")")) {
                    tokens.advance();
                    argumentCount++;
                    break;
                }
                expect(tokens.next(), ",");
            }

            return new Call(function, Arrays.copyOf(arguments, argumentCount));
        }
        else {
            Variable variable = context.getCurrentScope().variables.get(nextToken);
            Operand[] access = new Operand[8];
            byte dimensionCount = 0;

            // Array access
            if (tokens.peek().equals("[")) {
                while (tokens.peek().equals("[")) {
                    tokens.advance();
                    Operand expression = parseExpression();
                    if (expression.getResultType() != Primitive.INT) {
                        throw new ParserException("Not an integer!");
                    }

                    access[dimensionCount++] = expression;
                    expect(tokens.next(), "]");
                }
            } else {
                dimensionCount = 1;
            }

            if (variable.lengths.length != dimensionCount) {
                throw new ParserException("Array access must specify all array dimensions.");
            }

            if (prefix == 0) {
                if (tokens.peek().equals("++")) {
                    // is postfix
                    tokens.advance();
                    postfix = Unary.POSTFIX_INCREMENT;
                }
                else if (tokens.peek().equals("--")) {
                    tokens.advance();
                    postfix = Unary.POSTFIX_DECREMENT;
                }
            }

            Operand[] accessOperands = new Operand[dimensionCount];
            System.arraycopy(access, 0, accessOperands, 0, dimensionCount);

            if (variable != null) {
                if ((prefix | postfix) != 0) {
                    return new Unary(new Access(variable, accessOperands), (byte) (prefix | postfix));
                }
                return new Access(variable, accessOperands);
            }
        }

        throw new ParserException("Unknown member '" + nextToken + "'.");
    }

    Operand parseOperator(String raw) {
        return switch (raw) {
            case "+" -> Operator.ADD;
            case "-" -> Operator.SUB;
            case "*" -> Operator.MUL;
            case "/" -> Operator.DIV;

            case "&&" -> Operator.AND;
            case "||" -> Operator.OR;

            case "==" -> Operator.EQUALS;
            case "!=" -> Operator.NOT_EQUALS;
            case "<" -> Operator.LESS_THAN;
            case "<=" -> Operator.LESS_EQUALS;
            case ">" -> Operator.GREATER_THAN;
            case ">=" -> Operator.GREATER_EQUALS;

            case "=" -> Operator.ASSIGN;
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
        return parse(new ModuleRepository(), code);
    }

    public static AST parse(ModuleRepository repository, String code) throws ParserException {
        return new Parser(repository, Tokenizer.tokenize(code.split("\n"))).parse();
    }

}
