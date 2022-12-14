package com.valterc.ki2.data.device;

public enum DeviceType {

    SHIMANO_SHIFTING(1),
    UNKNOWN(255);

    public static DeviceType fromDeviceTypeValue(int deviceTypeValue) {
        for (DeviceType d : values()) {
            if (d.deviceTypeValue == deviceTypeValue) {
                return d;
            }
        }

        return UNKNOWN;
    }

    private final int deviceTypeValue;

    DeviceType(int deviceTypeValue) {
        this.deviceTypeValue = deviceTypeValue;
    }

    public int getDeviceTypeValue() {
        return deviceTypeValue;
    }
}
