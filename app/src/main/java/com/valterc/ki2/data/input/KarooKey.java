package com.valterc.ki2.data.input;

import android.view.KeyEvent;

public enum KarooKey {
    INVALID,

    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_RIGHT,
    BOTTOM_LEFT,
    ;

    public static KarooKey fromKeyCode(int keyCode) {
        return switch (keyCode) {
            case KeyEvent.KEYCODE_NAVIGATE_PREVIOUS -> TOP_LEFT;
            case KeyEvent.KEYCODE_NAVIGATE_NEXT -> TOP_RIGHT;
            case KeyEvent.KEYCODE_BACK -> BOTTOM_LEFT;
            case KeyEvent.KEYCODE_NAVIGATE_IN -> BOTTOM_RIGHT;
            default -> INVALID;
        };
    }
}
