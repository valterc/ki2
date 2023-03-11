package com.valterc.ki2.data.audio;

public enum AudioAlertEvent {

    NONE,
    SHIFT_LOWEST_GEAR,
    SHIFT_HIGHEST_GEAR,
    SHIFT_LIMIT,
    UPCOMING_SYNCHRO_SHIFT;

    public static AudioAlertEvent fromOrdinal(int ordinal) {
        for (AudioAlertEvent value : values()) {
            if (value.ordinal() == ordinal) {
                return value;
            }
        }

        return NONE;
    }

}
