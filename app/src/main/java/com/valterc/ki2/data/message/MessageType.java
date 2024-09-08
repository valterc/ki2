package com.valterc.ki2.data.message;

public enum MessageType {

    UNKNOWN(0),
    LOW_BATTERY(1),
    RIDE_STATUS(2),
    UPDATE_AVAILABLE(3),
    SHOW_OVERLAY(4),
    AUDIO_ALERT(5),
    AUDIO_ALERT_EVENT(6),
    AUDIO_ALERT_TOGGLE(7),
    AUDIO_ALERT_DISABLE(8),
    AUDIO_ALERT_ENABLE(9),
    ENABLE_ANT(10);

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
