package com.github.alantr7.codebots.cbslang.low.runtime.modules;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public abstract class Module {

    @Getter
    private final String name;

    private final Map<String, ExternalFunction> handlers = new HashMap<>();

    public Module(String name) {
        this.name = name;
        setup();
    }

    public abstract void setup();

    protected void registerFunction(String name, ExternalFunction handler) {
        handlers.put(name, handler);
    }

    public ExternalFunction getFunction(String name) {
        return handlers.get(name);
    }

}
