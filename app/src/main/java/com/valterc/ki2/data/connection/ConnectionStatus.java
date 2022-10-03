package com.valterc.ki2.data.connection;

import java.util.Arrays;
import java.util.Optional;

public enum ConnectionStatus {

    INVALID(0),
    NEW(1),
    CONNECTING(2),
    ESTABLISHED(3),
    CLOSED(4);

    public static ConnectionStatus fromValue(int value) {
        Optional<ConnectionStatus> connectionStatus =
                Arrays.stream(values())
                        .filter(d -> d.value == value)
                        .findFirst();

        return connectionStatus.orElse(INVALID);
    }

    private final int value;

    ConnectionStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
