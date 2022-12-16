package com.valterc.ki2.data.command;

public enum CommandType {

    SHIFTING_MODE(1),
    UNKNOWN(255);

    public static CommandType fromValue(int commandValue) {
        for (CommandType commandType : values()) {
            if (commandType.commandValue == commandValue) {
                return commandType;
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
