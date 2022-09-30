package com.valterc.ki2.data.switches;

public enum SwitchCommandType {

    NONE(1),
    SINGLE_PRESS(2),
    DOUBLE_PRESS(3),
    HOLD(4);

    private final int value;

    SwitchCommandType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public SwitchCommand getCommand() {
        switch (this) {
            case SINGLE_PRESS:
                return SwitchCommand.SINGLE_CLICK;

            case DOUBLE_PRESS:
                return SwitchCommand.DOUBLE_CLICK;
        }

        return SwitchCommand.NO_SWITCH;
    }
}
