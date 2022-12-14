package com.valterc.ki2.data.message;

public enum MessageType {

    UNKNOWN(0),
    LOW_BATTERY(1),
    RIDE_STATUS(2),
    UPDATE_AVAILABLE(3),

    OTHER(255);

    public static MessageType fromValue(int value) {
        for (MessageType d : values()) {
            if (d.value == value) {
                return d;
            }
        }

        return UNKNOWN;
    }

    private final int value;

    MessageType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
