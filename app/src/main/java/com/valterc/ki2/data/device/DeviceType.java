package com.valterc.ki2.data.device;

import java.util.Arrays;
import java.util.Optional;

public enum DeviceType {

    SHIMANO_SHIFTING(1),
    UNKNOWN(255);

    public static DeviceType fromDeviceTypeValue(int deviceTypeValue) {
        Optional<DeviceType> deviceType =
                Arrays.stream(values())
                        .filter(d -> d.deviceTypeValue == deviceTypeValue)
                        .findFirst();

        return deviceType.orElse(UNKNOWN);
    }

    private final int deviceTypeValue;

    DeviceType(int deviceTypeValue) {
        this.deviceTypeValue = deviceTypeValue;
    }

    public int getDeviceTypeValue() {
        return deviceTypeValue;
    }
}
