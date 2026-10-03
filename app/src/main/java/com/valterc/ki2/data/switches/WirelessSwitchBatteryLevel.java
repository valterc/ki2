package com.valterc.ki2.data.switches;

public enum WirelessSwitchBatteryLevel {

    UNKNOWN(0),
    LOW(1),
    MID(2),
    HIGH(3);

    public static WirelessSwitchBatteryLevel fromValue(int value) {
        for (WirelessSwitchBatteryLevel wirelessSwitchBatteryLevel : values()) {
            if (wirelessSwitchBatteryLevel.value == value) {
                return wirelessSwitchBatteryLevel;
            }
        }

        return UNKNOWN;
    }

    private final int value;

    WirelessSwitchBatteryLevel(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
