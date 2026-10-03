package com.valterc.ki2.data.switches;

public enum SwitchType {

    NONE(0),
    ROAD_SHIFTER(1),
    TT_SHIFTER(2),
    TT_SWITCH(3),
    MTB_SWITCH(4),
    SWITCH(5),
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
