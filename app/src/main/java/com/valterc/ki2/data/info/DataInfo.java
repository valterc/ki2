package com.valterc.ki2.data.info;

import android.os.Parcel;
import android.os.Parcelable;

public class DataInfo implements Parcelable {

    private DataType type;
    private long timestamp;
    private Parcelable value;

    public static final Parcelable.Creator<DataInfo> CREATOR = new Parcelable.Creator<DataInfo>() {
        public DataInfo createFromParcel(Parcel in) {
            return new DataInfo(in);
        }

        public DataInfo[] newArray(int size) {
            return new DataInfo[size];
        }
    };

    private DataInfo(Parcel in) {
        readFromParcel(in);
    }

    public DataInfo(DataType type, long timestamp, Parcelable value) {
        this.type = type;
        this.timestamp = timestamp;
        this.value = value;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(type.getFlag());
        out.writeLong(timestamp);
        out.writeParcelable(value, flags);
    }

    public void readFromParcel(Parcel in) {
        type = DataType.fromFlag(in.readInt());
        timestamp = in.readLong();
        value = in.readParcelable(getClass().getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
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
}
