package com.github.alantr7.codebots.cbslang.high.compiler;

import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.Function;

import java.util.Stack;

public class CompilerContext {

    Function currentFunction;

    Stack<LoopContext> loopStack = new Stack<>();

}
