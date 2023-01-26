package com.valterc.ki2.data.device;

public enum DeviceType {

    SHIMANO_SHIFTING(1),
    UNKNOWN(1024),
    MOCK_SHIFTING(UNKNOWN.value + 1);

    public static DeviceType fromDeviceTypeValue(int value) {
        for (DeviceType deviceType : values()) {
            if (deviceType.value == value) {
                return deviceType;
            }
        }

        return UNKNOWN;
    }

    private final int value;

    DeviceType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
