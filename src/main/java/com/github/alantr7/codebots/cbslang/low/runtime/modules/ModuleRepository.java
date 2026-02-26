package com.github.alantr7.codebots.cbslang.low.runtime.modules;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ModuleRepository {

    private final Map<String, Module> modules = new HashMap<>();

    public static final ModuleRepository EMPTY = new ModuleRepository();

    public Module getModule(String name) {
        return modules.get(name);
    }

    public Collection<Module> getModules() {
        return modules.values();
    }

    public void registerModule(Module module) {
        modules.put(module.getName(), module);
    }


}
