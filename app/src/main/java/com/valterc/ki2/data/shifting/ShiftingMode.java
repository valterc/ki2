package com.valterc.ki2.data.shifting;

import java.util.Arrays;
import java.util.Optional;

public enum ShiftingMode {

    NORMAL(0, "M"),
    ADJUSTMENT(1, "..."),
    RESETTING(2, "..."),
    SYNCHRONIZED_SHIFT_MODE_1(3, "S1"),
    SYNCHRONIZED_SHIFT_MODE_2(4, "S2"),
    INVALID(255, "");

    public static ShiftingMode fromValue(int value) {
        Optional<ShiftingMode> element =
                Arrays.stream(ShiftingMode.values()).filter(s -> s.value == value)
                .findFirst();

        return element.orElse(INVALID);
    }

    private final int value;
    private final String mode;

    ShiftingMode(int value, String mode) {
        this.value = value;
        this.mode = mode;
    }

    public final String getMode() {
        return this.mode;
    }

    public final int getValue() {
        return this.value;
    }

}
