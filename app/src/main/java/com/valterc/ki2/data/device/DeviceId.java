package com.valterc.ki2.data.device;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class DeviceId implements Parcelable {

    private final int deviceNumber;
    private final int deviceTypeValue;
    private final int transmissionType;
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
        deviceNumber = in.readInt();
        deviceTypeValue = in.readInt();
        transmissionType = in.readInt();
        deviceType = DeviceType.fromDeviceTypeValue(deviceTypeValue);
    }

    public DeviceId(int deviceNumber, int deviceTypeValue, int transmissionType) {
        this.deviceNumber = deviceNumber;
        this.deviceTypeValue = deviceTypeValue;
        this.transmissionType = transmissionType;
        this.deviceType = DeviceType.fromDeviceTypeValue(deviceTypeValue);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(deviceNumber);
        out.writeInt(deviceTypeValue);
        out.writeInt(transmissionType);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Get a simple string with a unique identifier representation of this device identifier.
     *
     * @return String with unique identifier representation of this device identifier.
     */
    @NonNull
    public String getUid() {
        return deviceNumber + "-" + deviceTypeValue + "-" + transmissionType;
    }

    public int getDeviceNumber() {
        return deviceNumber;
    }

    public int getDeviceTypeValue() {
        return deviceTypeValue;
    }

    public int getTransmissionType() {
        return transmissionType;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeviceId deviceId = (DeviceId) o;

        if (deviceNumber != deviceId.deviceNumber) return false;
        if (deviceTypeValue != deviceId.deviceTypeValue) return false;
        return transmissionType == deviceId.transmissionType;
    }

    @Override
    public int hashCode() {
        int result = deviceNumber;
        result = 31 * result + deviceTypeValue;
        result = 31 * result + transmissionType;
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "DeviceId{" +
                "deviceNumber=" + deviceNumber +
                ", deviceTypeValue=" + deviceTypeValue +
                ", transmissionType=" + transmissionType +
                ", deviceType=" + deviceType +
                '}';
    }
}
