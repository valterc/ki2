package com.valterc.ki2.data.device;

public enum ShimanoPageType {

    BATTERY_LEVEL_AND_NUMBER_OF_SPEEDS(0),
    RESERVED(1),
    GEAR_SHIFTING_ADJUSTMENT(2),
    SUSPENSION_STATUS(3),
    SWITCH_STATUS(4),
    BUZZER_NOTIFICATION(5),
    SWITCH_DFLY_CH1(6),
    SWITCH_DFLY_CH2(7),
    SWITCH_DFLY_CH3(8),
    SWITCH_DFLY_CH4(9),
    SYSTEM_FUNCTIONS(10),
    CHAINRINGS(11),
    BIKE_STATUS(17),
    REQUEST_SHIFT_MODE_TRANSITION(48),
    SHIFT_MODE_TRANSITION_ACK(49),
    MANUFACTURER_INFORMATION(80),
    PRODUCT_INFORMATION(81),
    ANT_SLAVE_STATUS(128),
    UNKNOWN(65535);

    public static ShimanoPageType fromPageNumber(int pageNumber) {
        for (ShimanoPageType s : ShimanoPageType.values()) {
            if (s.pageNumber == pageNumber) {
                return s;
            }
        }

        return UNKNOWN;
    }

    private final int pageNumber;

    ShimanoPageType(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public final int getPageNumber() {
        return this.pageNumber;
    }


}
