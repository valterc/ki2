package com.valterc.ki2.data.device;

import com.dsi.ant.message.ChannelId;

public enum DeviceType {

    SHIMANO_SHIFTING(1),
    UNKNOWN(ChannelId.MAX_DEVICE_TYPE + 1),
    MOCK_SHIFTING(UNKNOWN.value + 1);

    public static final int MOCK_SHIFTING_VALUE = MOCK_SHIFTING.value;

    /**
     * Get the {@link DeviceType} for the specified device type value. If the device type value is unrecognized then {@link DeviceType#UNKNOWN} is returned.
     * @param value Device type value.
     * @return DeviceType or {@link DeviceType#UNKNOWN} if the value is unrecognized.
     */
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

}
