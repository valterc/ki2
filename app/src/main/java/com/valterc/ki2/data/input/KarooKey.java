package com.valterc.ki2.data.input;

import android.view.KeyEvent;

import java.util.Arrays;
import java.util.Optional;

/**
 * Represents a Karoo hardware key and the corresponding KeyEvent keycode.
 */
public enum KarooKey{

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
    NONE(KeyEvent.KEYCODE_UNKNOWN);

    public static KarooKey fromKeyCode(int keyCode) {
        Optional<KarooKey> element =
                Arrays.stream(KarooKey.values()).filter(s -> s.keyCode == keyCode)
                        .findFirst();

        return element.orElse(NONE);
    }

    private final int keyCode;

    KarooKey(int keyCode) {
        this.keyCode = keyCode;
    }

    public final int getKeyCode() {
        return this.keyCode;
    }

}
