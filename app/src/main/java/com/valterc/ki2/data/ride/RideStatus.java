package com.valterc.ki2.data.ride;

import java.util.Arrays;
import java.util.Optional;

public enum RideStatus {

    NEW(0),
    ONGOING(1),
    PAUSED(2),
    FINISHED(3);

    public static RideStatus fromValue(int value) {
        Optional<RideStatus> optionalValue =
                Arrays.stream(values())
                        .filter(d -> d.value == value)
                        .findFirst();

        return optionalValue.orElse(NEW);
    }

    private final int value;

    RideStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
