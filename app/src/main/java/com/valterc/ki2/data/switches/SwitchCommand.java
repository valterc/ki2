package com.valterc.ki2.data.switches;

import com.valterc.ki2.data.action.KeyAction;

public enum SwitchCommand {

    SINGLE_CLICK(16),
    LONG_PRESS_DOWN(32),
    LONG_PRESS_CONTINUE(48),
    LONG_PRESS_UP(0),
    DOUBLE_CLICK(64),
    NO_SWITCH(240);

    public static SwitchCommand fromCommandNumber(int commandNumber) {
        for (SwitchCommand switchCommand : SwitchCommand.values()) {
            if (switchCommand.commandNumber == commandNumber) {
                return switchCommand;
            }
        }

        return NO_SWITCH;
    }

    private final int commandNumber;

    SwitchCommand(int commandNumber) {
        this.commandNumber = commandNumber;
    }

    public final int getCommandNumber() {
        return this.commandNumber;
    }

    public final KeyAction getKeyAction() {
        switch (this) {

            case SINGLE_CLICK:
                return KeyAction.SINGLE_PRESS;

            case DOUBLE_CLICK:
                return KeyAction.DOUBLE_PRESS;

            case LONG_PRESS_DOWN:
                return KeyAction.LONG_PRESS_DOWN;

            case LONG_PRESS_CONTINUE:
                return KeyAction.LONG_PRESS_CONTINUE;

            case LONG_PRESS_UP:
                return KeyAction.LONG_PRESS_UP;

        }

        return KeyAction.NO_ACTION;
    }

    public final SwitchCommandType getCommandType() {
        switch (this) {

            case SINGLE_CLICK:
                return SwitchCommandType.SINGLE_PRESS;

            case DOUBLE_CLICK:
                return SwitchCommandType.DOUBLE_PRESS;

            case LONG_PRESS_DOWN:
            case LONG_PRESS_CONTINUE:
            case LONG_PRESS_UP:
                return SwitchCommandType.HOLD;

        }

        return SwitchCommandType.NONE;
    }

}
