package com.valterc.ki2.data.action;

/**
 * Represents a Karoo hardware or virtual key and the corresponding KeyEvent keycode.
 */
public enum KarooAction {

    INVALID,

    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_RIGHT,
    BOTTOM_LEFT,

    VIRTUAL_SWITCH_TO_MAP_PAGE,
    VIRTUAL_TOGGLE_OVERLAY,
    VIRTUAL_HIDE_OVERLAY,
    VIRTUAL_SHOW_OVERLAY,
    VIRTUAL_TURN_SCREEN_ON,
    VIRTUAL_TURN_SCREEN_OFF,
    VIRTUAL_TOGGLE_AUDIO_ALERTS,
    VIRTUAL_DISABLE_AUDIO_ALERTS,
    VIRTUAL_ENABLE_AUDIO_ALERTS,
    VIRTUAL_SINGLE_BEEP,
    VIRTUAL_DOUBLE_BEEP,
    VIRTUAL_BELL,
    VIRTUAL_LAP,
    VIRTUAL_ZOOM_OUT,
    VIRTUAL_ZOOM_IN,
    VIRTUAL_CONTROL_CENTER,
    VIRTUAL_DRAWER_ACTION;

    private static final KarooAction[] values = KarooAction.values();

    public static KarooAction fromOrdinal(int ordinal) {
        if (ordinal < 0 || ordinal >= values.length) {
            return INVALID;
        }

        return values[ordinal];
    }
}
