package com.valterc.ki2.data.command;

import java.util.Arrays;
import java.util.Optional;

public enum CommandType {

    SHIFTING_MODE(1),
    UNKNOWN(255);

    public static CommandType fromValue(int commandValue) {
        Optional<CommandType> element =
                Arrays.stream(CommandType.values()).filter(s -> s.commandValue == commandValue)
                        .findFirst();

        return element.orElse(UNKNOWN);
    }

    private final int commandValue;

    CommandType(int commandValue) {
        this.commandValue = commandValue;
    }

    public int getCommandValue() {
        return commandValue;
    }
}
