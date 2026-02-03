package com.github.alantr7.codebots.cbslang.low.runtime.memory;

import lombok.Getter;

public class Data {

    @Getter
    private DataType<?> dataType;

    @Getter
    private Object value;

    public Data() {
    }

    public Data(DataType<?> dataType, Object value) {
        this.dataType = dataType;
        this.value = value;
    }

    public <T> void setValue(DataType<T> type, T value) {
        this.dataType = type;
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    public <T> T getValueAs(DataType<T> type) {
        return (T) value;
    }

}
