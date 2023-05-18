package com.valterc.ki2.data.switches;

public enum SwitchType {

    D_FLY_CH1(1),
    D_FLY_CH2(2),
    D_FLY_CH3(3),
    D_FLY_CH4(4),
    UNKNOWN(255);

    public static SwitchType fromValue(int value) {
        for (SwitchType switchType : values()) {
            if (switchType.value == value) {
                return switchType;
            }
        }

        return UNKNOWN;
    }

    private final int value;

    SwitchType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
