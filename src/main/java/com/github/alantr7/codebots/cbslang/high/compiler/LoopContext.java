package com.github.alantr7.codebots.cbslang.high.compiler;

public class LoopContext {

    public String nextIterationId;

    public String exitId;

    public LoopContext(String nextIterationId, String exitId) {
        this.nextIterationId = nextIterationId;
        this.exitId = exitId;
    }

}
