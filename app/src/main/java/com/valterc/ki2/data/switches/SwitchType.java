package com.valterc.ki2.data.switches;

public enum SwitchType {

    LEFT(1),
    RIGHT(2),
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
