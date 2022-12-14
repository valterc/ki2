package com.valterc.ki2.data.connection;

public enum ConnectionStatus {

    INVALID(0),
    NEW(1),
    CONNECTING(2),
    ESTABLISHED(3),
    CLOSED(4);

    public static ConnectionStatus fromValue(int value) {
        for (ConnectionStatus d : values()) {
            if (d.value == value) {
                return d;
            }
        }

        return INVALID;
    }

    private final int value;

    ConnectionStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
