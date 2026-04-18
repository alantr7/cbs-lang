package com.github.alantr7.codebots.cbslang.high.compiler;

import com.github.alantr7.codebots.cbslang.exceptions.ParserException;
import com.github.alantr7.codebots.cbslang.high.parser.Parser;
import com.github.alantr7.codebots.cbslang.high.parser.ast.AST;
import com.github.alantr7.codebots.cbslang.high.parser.ast.expressions.*;
import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.Function;
import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.FunctionSignature;
import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.Primitive;
import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.Scope;
import com.github.alantr7.codebots.cbslang.high.parser.ast.statements.*;
import com.github.alantr7.codebots.cbslang.low.runtime.Program;
import com.github.alantr7.codebots.cbslang.low.runtime.ProgramExecutor;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.ModuleRepository;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public abstract class Compiler<T> {

    protected final CompilerContext context = new CompilerContext();

    public final AST ast;

    public Compiler(AST ast) {
        this.ast = ast;
    }

    @Getter
    private final Scope globalScope = new Scope();

    public abstract void compile();

    public abstract T getOutput();

    public static String toHumanReadable(ModuleRepository modules, String code) throws ParserException {
        HumanReadableCompiler compiler = new HumanReadableCompiler(Parser.parse(modules, code));
        compiler.compile();

        return compiler.getOutput();
    }

    public static byte[] toBytecode(ModuleRepository modules, String code) throws ParserException {
        ByteCodeCompiler compiler = new ByteCodeCompiler(Parser.parse(modules, code));
        compiler.compile();

        return compiler.getOutput();
    }

}
