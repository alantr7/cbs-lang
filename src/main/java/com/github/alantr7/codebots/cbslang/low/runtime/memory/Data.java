package com.github.alantr7.codebots.cbslang.low.runtime.memory;

import lombok.Getter;

import java.util.function.Function;

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

    public void copyFrom(Data other) {
        if (other != null) {
            this.dataType = other.dataType;
            this.value = other.value;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> void updateValue(DataType<T> type, Function<T, T> updateFunction) {
        value = updateFunction.apply((T) value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getValueAs(DataType<T> type) {
        return (T) value;
    }

    public static Data of(int integer) {
        return new Data(DataType.INT, integer);
    }

    public static Data of(long number) {
        return new Data(DataType.LONG, number);
    }

    public static Data of(float number) {
        return new Data(DataType.FLOAT, number);
    }

    public static Data of(String string) {
        return new Data(DataType.STRING, string);
    }

}
