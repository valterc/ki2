package com.valterc.ki2.data.message;

public enum MessageType {

    UNKNOWN(0),
    LOW_BATTERY(1),
    RIDE_STATUS(2),
    UPDATE_AVAILABLE(3),
    SHOW_OVERLAY(4);

    public static MessageType fromValue(int value) {
        for (MessageType messageType : values()) {
            if (messageType.value == value) {
                return messageType;
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
