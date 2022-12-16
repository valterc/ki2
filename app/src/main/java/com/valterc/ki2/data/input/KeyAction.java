package com.valterc.ki2.data.input;

public enum KeyAction {

    SINGLE_PRESS(0),
    DOUBLE_PRESS(1),
    LONG_PRESS_DOWN(2),
    LONG_PRESS_CONTINUE(3),
    LONG_PRESS_UP(4),
    SIMULATE_LONG_PRESS(5),
    NO_ACTION(255);

    public static KeyAction fromActionNumber(int actionNumber) {
        for (KeyAction s : values()) {
            if (s.actionNumber == actionNumber) {
                return s;
            }
        }

        return NO_ACTION;
    }

    private final int actionNumber;

    KeyAction(int actionNumber) {
        this.actionNumber = actionNumber;
    }

    public final int getActionNumber() {
        return this.actionNumber;
    }

}
