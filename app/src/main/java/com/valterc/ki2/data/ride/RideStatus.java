package com.valterc.ki2.data.ride;

public enum RideStatus {

    NEW(0),
    ONGOING(1),
    PAUSED(2),
    FINISHED(3);

    public static RideStatus fromValue(int value) {
        for (RideStatus rideStatus : values()) {
            if (rideStatus.value == value) {
                return rideStatus;
            }
        }

        return NEW;
    }

    private final int value;

    RideStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
