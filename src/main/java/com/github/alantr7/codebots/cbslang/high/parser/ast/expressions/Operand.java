package com.github.alantr7.codebots.cbslang.high.parser.ast.expressions;

import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.Type;
import com.github.alantr7.codebots.cbslang.high.parser.ast.statements.ForInitExpr;

public interface Operand extends ForInitExpr {

    Type getResultType();

}
