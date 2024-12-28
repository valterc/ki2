package com.valterc.ki2.data.device;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class BatteryInfo implements Parcelable {

    private int value;

    public static final Parcelable.Creator<BatteryInfo> CREATOR = new Parcelable.Creator<BatteryInfo>() {
        public BatteryInfo createFromParcel(Parcel in) {
            return new BatteryInfo(in);
        }

        public BatteryInfo[] newArray(int size) {
            return new BatteryInfo[size];
        }
    };

    private BatteryInfo(Parcel in) {
        readFromParcel(in);
    }

    public BatteryInfo(int value) {
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

        BatteryInfo that = (BatteryInfo) o;

        return value == that.value;
    }

    @Override
    public int hashCode() {
        return value;
    }

    @NonNull
    @Override
    public String toString() {
        return "BatteryInfo{" +
                "value=" + value +
                '}';
    }
}
