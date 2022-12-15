package com.valterc.ki2.data.shifting;

public enum BuzzerPattern {

    DEFAULT(0),
    ON(1),
    OFF(2),
    OVERLIMIT_PROTECTION(3),
    UNKNOWN(255);

    public static BuzzerPattern fromCommandNumber(int commandNumber) {
        for (BuzzerPattern s : values()) {
            if (s.commandNumber == commandNumber) {
                return s;
            }
        }

        return UNKNOWN;
    }

    private final long commandNumber;

    BuzzerPattern(long commandNumber) {
        this.commandNumber = commandNumber;
    }

    public final long getCommandNumber() {
        return this.commandNumber;
    }


}
