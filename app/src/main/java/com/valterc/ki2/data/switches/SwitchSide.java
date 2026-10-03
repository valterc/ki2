package com.valterc.ki2.data.switches;

public enum SwitchSide {

    NONE(0),
    LEFT(1),
    RIGHT(2),
    LEFT_OR_RIGHT(3);

    private static final int FLAG_LEFT = 0x1;
    private static final int FLAG_RIGHT = 0x2;

    public static SwitchSide fromFlags(int flags) {
        return fromValue(flags & (FLAG_LEFT | FLAG_RIGHT));
    }

    public static SwitchSide fromValue(int value) {
        for (SwitchSide switchSide : values()) {
            if (switchSide.value == value) {
                return switchSide;
            }
        }

        return NONE;
    }

    private final int value;

    SwitchSide(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
