package com.valterc.ki2.data.command;

import java.util.Arrays;
import java.util.Optional;

public enum CommandType {

    SHIFTING_MODE(1),
    UNKNOWN(255);

    public static CommandType fromValue(int commandValue) {
        for (CommandType s : CommandType.values()) {
            if (s.commandValue == commandValue) {
                return s;
            }
        }

        return UNKNOWN;
    }

    private final int commandValue;

    CommandType(int commandValue) {
        this.commandValue = commandValue;
    }

    public int getCommandValue() {
        return commandValue;
    }
}
