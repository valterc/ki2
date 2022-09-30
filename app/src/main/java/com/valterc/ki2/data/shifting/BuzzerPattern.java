package com.valterc.ki2.data.shifting;

import java.util.Arrays;
import java.util.Optional;

public enum BuzzerPattern {

    DEFAULT(0),
    ON(1),
    OFF(2),
    OVERLIMIT_PROTECTION(3),
    UNKNOWN(255);

    public static BuzzerPattern fromCommandNumber(int commandNumber) {
        Optional<BuzzerPattern> element =
                Arrays.stream(BuzzerPattern.values()).filter(s -> s.commandNumber == commandNumber)
                        .findFirst();

        return element.orElse(UNKNOWN);
    }

    private final long commandNumber;

    BuzzerPattern(long commandNumber) {
        this.commandNumber = commandNumber;
    }

    public final long getCommandNumber() {
        return this.commandNumber;
    }


}
