package com.valterc.ki2.data.message;

import java.util.Arrays;
import java.util.Optional;

public enum MessageType {

    UNKNOWN(0),
    LOW_BATTERY(1),
    RIDE_STATUS(2),

    OTHER(255);

    public static MessageType fromValue(int value) {
        Optional<MessageType> optionalValue =
                Arrays.stream(values())
                        .filter(d -> d.value == value)
                        .findFirst();

        return optionalValue.orElse(UNKNOWN);
    }

    private final int value;

    MessageType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
