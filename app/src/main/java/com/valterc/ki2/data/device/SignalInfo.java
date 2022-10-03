package com.valterc.ki2.data.device;

import android.os.Parcel;
import android.os.Parcelable;

public class SignalInfo implements Parcelable {

    private int value;

    public static final Creator<SignalInfo> CREATOR = new Creator<SignalInfo>() {
        public SignalInfo createFromParcel(Parcel in) {
            return new SignalInfo(in);
        }

        public SignalInfo[] newArray(int size) {
            return new SignalInfo[size];
        }
    };

    private SignalInfo(Parcel in) {
        readFromParcel(in);
    }

    public SignalInfo(int value) {
        this.value = value;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(value);
    }

    public void readFromParcel(Parcel in) {
        value = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SignalInfo that = (SignalInfo) o;

        return value == that.value;
    }

    @Override
    public int hashCode() {
        return value;
    }
}
