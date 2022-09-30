package com.valterc.ki2.data.device;

import java.util.Arrays;
import java.util.Optional;

public enum StatusIndicator {

    NONE(0),
    BATTERY_INDICATOR(1),
    SYSTEM_STATUS(2),
    NUMBER_OF_UNITS_CONNECTED_TO_A_SWITCH(4),
    FRONT_SPEEDS_CURRENT(16),
    REAR_SPEEDS_CURRENT(32),
    FRONT_SPEEDS_MAX(64),
    REAR_SPEEDS_MAX(128),
    FRONT_ADJUSTMENT_CURRENT(256),
    REAR_ADJUSTMENT_CURRENT(512),
    FRONT_ADJUSTMENT_MAX(1024),
    REAR_ADJUSTMENT_MAX(2048),
    FRONT_ADJUSTMENT_MIN(4096),
    REAR_ADJUSTMENT_MIN(8192),
    SUSPENSION_POSITION(32768),
    SWITCH_COMMAND_NUMBER(65536);

    public static StatusIndicator fromFlag(int flag) {
        Optional<StatusIndicator> element =
                Arrays.stream(StatusIndicator.values()).filter(s -> s.flag == flag)
                        .findFirst();

        return element.orElse(NONE);
    }

    private final int flag;

    StatusIndicator(int flag) {
        this.flag = flag;
    }

    public final int getFlag() {
        return this.flag;
    }

}
