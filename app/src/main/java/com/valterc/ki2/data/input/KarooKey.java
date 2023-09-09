package com.valterc.ki2.data.input;

import android.view.KeyEvent;

/**
 * Represents a Karoo hardware or virtual key and the corresponding KeyEvent keycode.
 */
public enum KarooKey {

    /**
     * Left top key.
     */
    LEFT(KeyEvent.KEYCODE_NAVIGATE_PREVIOUS),

    /**
     * Right top key.
     */
    RIGHT(KeyEvent.KEYCODE_NAVIGATE_NEXT),

    /**
     * Right bottom key.
     */
    CONFIRM(KeyEvent.KEYCODE_NAVIGATE_IN),

    /**
     * Left bottom key.
     */
    BACK(KeyEvent.KEYCODE_BACK),

    /**
     * None, unknown or unassigned key.
     */
    NONE(KeyEvent.KEYCODE_UNKNOWN),

    /**
     * Virtual key representing no action.
     */
    VIRTUAL_NONE(10_000),

    /**
     * Virtual key to switch the ride activity to the map page.
     */
    VIRTUAL_SWITCH_TO_MAP_PAGE(VIRTUAL_NONE.keyCode + 1),

    /**
     * Virtual key to show ride overlay.
     */
    VIRTUAL_SHOW_OVERLAY(VIRTUAL_NONE.keyCode + 2),

    /**
     * Virtual key to turn screen on.
     */
    VIRTUAL_TURN_SCREEN_ON(VIRTUAL_NONE.keyCode + 3),

    /**
     * Virtual key to take screenshot.
     */
    VIRTUAL_TAKE_SCREENSHOT(VIRTUAL_NONE.keyCode + 4),

    /**
     * Virtual key to toggle audio alerts.
     */
    VIRTUAL_TOGGLE_AUDIO_ALERTS(VIRTUAL_NONE.keyCode + 5),

    /**
     * Virtual key to disable audio alerts.
     */
    VIRTUAL_DISABLE_AUDIO_ALERTS(VIRTUAL_NONE.keyCode + 6),

    /**
     * Virtual key to enable audio alerts.
     */
    VIRTUAL_ENABLE_AUDIO_ALERTS(VIRTUAL_NONE.keyCode + 7),

    /**
     * Virtual key to do single beep.
     */
    VIRTUAL_SINGLE_BEEP(VIRTUAL_NONE.keyCode + 8),

    /**
     * Virtual key to do double beep.
     */
    VIRTUAL_DOUBLE_BEEP(VIRTUAL_NONE.keyCode + 9);

    public static KarooKey fromKeyCode(int keyCode) {
        for (KarooKey karooKey : values()) {
            if (karooKey.keyCode == keyCode) {
                return karooKey;
            }
        }

        return NONE;
    }

    private final int keyCode;

    KarooKey(int keyCode) {
        this.keyCode = keyCode;
    }

    public final boolean isVirtual() {
        return keyCode >= VIRTUAL_NONE.keyCode;
    }

    public final int getKeyCode() {
        return this.keyCode;
    }

}
