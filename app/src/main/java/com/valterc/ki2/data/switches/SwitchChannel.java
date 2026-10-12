package com.valterc.ki2.data.switches;

public enum SwitchChannel {

    D_FLY_CH1(1),
    D_FLY_CH2(2),
    D_FLY_CH3(3),
    D_FLY_CH4(4),
    UNKNOWN(255);

    public static SwitchChannel fromValue(int value) {
        for (SwitchChannel switchChannel : values()) {
            if (switchChannel.value == value) {
                return switchChannel;
            }
        }

        return UNKNOWN;
    }

    private final int value;

    SwitchChannel(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
