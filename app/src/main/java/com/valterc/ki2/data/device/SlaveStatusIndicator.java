package com.valterc.ki2.data.device;

public enum SlaveStatusIndicator {

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
    SWITCH_COMMAND_NUMBER(65536),
    SWITCH_DFLY_CH1(131072),
    SWITCH_DFLY_CH2(262144),
    SWITCH_DFLY_CH3(524288),
    SWITCH_DFLY_CH4(1048576),
    SYSTEM_FUNCTIONS(2097152),
    CHAINRINGS(4194304);

    public static SlaveStatusIndicator fromFlag(int flag) {
        for (SlaveStatusIndicator slaveStatusIndicator : values()) {
            if (slaveStatusIndicator.flag == flag) {
                return slaveStatusIndicator;
            }
        }

        return NONE;
    }

    private final int flag;

    SlaveStatusIndicator(int flag) {
        this.flag = flag;
    }

    public final int getFlag() {
        return this.flag;
    }

}
