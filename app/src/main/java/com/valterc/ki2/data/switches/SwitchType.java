package com.valterc.ki2.data.switches;

import java.util.Arrays;
import java.util.Optional;

public enum SwitchType {

    LEFT(1),
    RIGHT(2),
    UNKNOWN(255);

    public static SwitchType fromValue(int value) {
        Optional<SwitchType> element =
                Arrays.stream(SwitchType.values()).filter(s -> s.value == value)
                        .findFirst();

        return element.orElse(UNKNOWN);
    }

    private final int value;

    SwitchType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
