package com.valterc.ki2.data.switches;

import java.util.Arrays;
import java.util.Optional;

public enum SwitchCommand {

    SINGLE_CLICK(16),
    LONG_PRESS_DOWN(32),
    LONG_PRESS_CONTINUE(48),
    LONG_PRESS_UP(0),
    DOUBLE_CLICK(64),
    NO_SWITCH(240);

    public static SwitchCommand fromCommandNumber(int commandNumber) {
        Optional<SwitchCommand> element =
                Arrays.stream(SwitchCommand.values()).filter(s -> s.commandNumber == commandNumber)
                        .findFirst();

        return element.orElse(NO_SWITCH);
    }

    private final int commandNumber;

    SwitchCommand(int commandNumber) {
        this.commandNumber = commandNumber;
    }

    public final int getCommandNumber() {
        return this.commandNumber;
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
