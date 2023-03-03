package com.valterc.ki2.data.message;

import android.os.Bundle;

import com.valterc.ki2.data.ride.RideStatus;

public class RideStatusMessage extends Message {
    private static final String KEY_RIDE_STATUS = "rideStatus";
    public static final String KEY = "ride-status";

    public static RideStatusMessage parse(Message message) {
        if (!message.getClassType().equals(RideStatusMessage.class.getName())) {
            return null;
        }

        return new RideStatusMessage(message);
    }

    private final RideStatus rideStatus;

    private RideStatusMessage(Message message) {
        super(message);
        rideStatus = RideStatus.fromValue(getBundle().getInt(KEY_RIDE_STATUS));
    }

    public RideStatusMessage(RideStatus rideStatus) {
        super(KEY, new Bundle(), MessageType.RIDE_STATUS, true);

        getBundle().putInt(KEY_RIDE_STATUS, rideStatus.getValue());
        this.rideStatus = rideStatus;
    }

    public RideStatus getRideStatus() {
        return rideStatus;
    }

}
