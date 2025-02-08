package com.valterc.ki2.data.message;

public enum MessageType {

    UNKNOWN,
    LOW_BATTERY,
    RIDE_STATUS,
    UPDATE_AVAILABLE,
    TOGGLE_OVERLAY,
    HIDE_OVERLAY,
    SHOW_OVERLAY,
    AUDIO_ALERT,
    AUDIO_ALERT_TOGGLE,
    AUDIO_ALERT_DISABLE,
    AUDIO_ALERT_ENABLE;

    public static MessageType fromOrdinal(int ordinal) {
        for (MessageType messageType : values()) {
            if (messageType.ordinal() == ordinal) {
                return messageType;
            }
        }

        return UNKNOWN;
    }

}
