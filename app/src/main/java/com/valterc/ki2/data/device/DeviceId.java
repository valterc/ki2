package com.valterc.ki2.data.device;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

public class DeviceId implements Parcelable {

    private final String uid;
    private final DeviceType deviceType;

    public static final Parcelable.Creator<DeviceId> CREATOR = new Parcelable.Creator<DeviceId>() {
        public DeviceId createFromParcel(Parcel in) {
            return new DeviceId(in);
        }

        public DeviceId[] newArray(int size) {
            return new DeviceId[size];
        }
    };

    private DeviceId(Parcel in) {
        uid = in.readString();
        deviceType = DeviceType.fromDeviceTypeValue(in.readInt());
    }

    public DeviceId(String uid, DeviceType deviceType) {
        this.uid = uid;
        this.deviceType = deviceType;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(uid);
        out.writeInt(deviceType.getDeviceTypeValue());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getUid() {
        return uid;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public Integer getAntDeviceId() {
        if (this.uid.contains("-")) {
            try {
                return Integer.parseInt(uid.substring(0, uid.indexOf('-')));
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    public String getName(){
        Integer antDeviceId = getAntDeviceId();
        if (antDeviceId != null) {
            return antDeviceId.toString();
        }

        return getUid();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeviceId deviceId = (DeviceId) o;

        if (!Objects.equals(uid, deviceId.uid)) return false;
        return deviceType == deviceId.deviceType;
    }

    @Override
    public int hashCode() {
        int result = uid != null ? uid.hashCode() : 0;
        result = 31 * result + (deviceType != null ? deviceType.hashCode() : 0);
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "DeviceId{" +
                "deviceType=" + deviceType +
                ", uid='" + uid + '\'' +
                ", antId='" + getAntDeviceId() + '\'' +
                '}';
    }
}
