package com.github.alantr7.codebots.cbslang.low.runtime.modules.standard;

import com.github.alantr7.codebots.cbslang.low.runtime.memory.Data;
import com.github.alantr7.codebots.cbslang.low.runtime.memory.DataType;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.Context;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.ExternalFunction;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.Module;

public class SystemModule extends Module {

    public SystemModule() {
        super("system");
    }

    @Override
    public void setup() {
        registerFunction("print", new ExternalFunction(this, "print", DataType.VOID, DataType.STRING) {
            @Override
            public Data handle(Context context) {
                return print(context);
            }
        });

        registerFunction("random", new ExternalFunction(this, "random", DataType.INT) {
            @Override
            public Data handle(Context context) {
                return new Data(DataType.INT, (int) (Math.random() * 500));
            }
        });

        registerFunction("sleep", new ExternalFunction(this, "sleep", DataType.VOID) {
            @Override
            public Data handle(Context context) {
                if (context.getMemory()[0] == null) {
                    context.getMemory()[0] = new Data(DataType.LONG, (System.currentTimeMillis() + context.getArguments()[0].getValueAs(DataType.INT)));
                    context.setRecall(true);
                } else {
                    if (System.currentTimeMillis() > context.getMemory()[0].getValueAs(DataType.LONG)) {
                        context.setRecall(false);
                    }
                }
                return null;
            }
        });
    }

    public Data print(Context context) {
        System.out.println();
        System.out.println("Print called!");
        System.out.println("My arguments:");
        for (Data argument : context.getArguments()) {
            System.out.println(" -> " + argument.getDataType().getTypeName() + " " + argument.getValue());
        }
        System.out.println();
        return null;
    }

}
