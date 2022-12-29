package com.valterc.ki2.data.shifting;

public enum BuzzerType {

    DEFAULT(0),
    ON(1),
    OFF(2),
    OVERLIMIT_PROTECTION(3),
    UPCOMING_SYNCHRO_SHIFT(4),
    UNKNOWN(255);

    public static BuzzerType fromCommandNumber(int commandNumber) {
        for (BuzzerType s : values()) {
            if (s.commandNumber == commandNumber) {
                return s;
            }
        }

        return UNKNOWN;
    }

    private final int commandNumber;

    BuzzerType(int commandNumber) {
        this.commandNumber = commandNumber;
    }

    public final int getCommandNumber() {
        return this.commandNumber;
    }


}
