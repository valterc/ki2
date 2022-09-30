package com.valterc.ki2.data.info;

import android.os.Parcelable;

public class DataInfoBuilder {

    private final DataType type;

    private long timestamp;
    private Parcelable value;

    public DataInfoBuilder(DataType type) {
        this.type = type;
    }

    public DataInfoBuilder(DataType dataType, Parcelable data) {
        this(dataType);
        this.value = data;
    }

    public DataType getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Parcelable getValue() {
        return value;
    }

    public void setValue(Parcelable value) {
        this.value = value;
        this.timestamp = System.currentTimeMillis();
    }

    public DataInfo buildDataInfo(){
        return new DataInfo(type, timestamp, value);
    }
}
