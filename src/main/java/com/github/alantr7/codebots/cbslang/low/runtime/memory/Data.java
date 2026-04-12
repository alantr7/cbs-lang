package com.github.alantr7.codebots.cbslang.low.runtime.memory;

import com.github.alantr7.bytils.buffer.ByteArrayReader;
import com.github.alantr7.bytils.buffer.ByteArrayWriter;
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

    @SuppressWarnings("unchecked")
    public void serialize(ByteArrayWriter buffer) {
        buffer.writeU1(dataType.getSerializationId());
        serialize(buffer, (DataType<Object>) dataType, value);
    }

    public static <T> void serialize(ByteArrayWriter buffer, DataType<T> dataType, T value) {
        buffer.writeU1(dataType.getSerializationId());
        switch (dataType.getSerializationId()) {
            case 1 -> buffer.writeInt((int) value);
            case 2 -> buffer.writeLong((long) value);
            case 3 -> buffer.writeInt(Float.floatToIntBits((float) value));
            case 4 -> buffer.writeString((String) value);
        }
    }

    public static Data deserialize(ByteArrayReader buffer) {
        int id = buffer.readU1();
        return switch (id) {
            case 1 -> new Data(DataType.INT, buffer.readInt());
            case 2 -> new Data(DataType.LONG, buffer.readLong());
            case 3 -> new Data(DataType.FLOAT, Float.intBitsToFloat(buffer.readInt()));
            case 4 -> new Data(DataType.STRING, buffer.readString());
            default -> null;
        };
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
