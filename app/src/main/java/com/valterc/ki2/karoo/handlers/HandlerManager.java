package com.valterc.ki2.karoo.handlers;

import com.valterc.ki2.data.message.RideStatusMessage;
import com.valterc.ki2.data.ride.RideStatus;
import com.valterc.ki2.karoo.service.ServiceClient;

import java.util.List;

public class HandlerManager {

    private final List<IRideHandler> rideHandlerList;
    private RideStatus rideStatus;

    public HandlerManager(ServiceClient serviceClient, List<IRideHandler> rideHandlerList) {
        this.rideHandlerList = rideHandlerList;
        this.rideStatus = RideStatus.NEW;

        serviceClient.getCustomMessageClient().registerRideStatusWeakListener(this::onRideStatusMessage);
    }

    private void onRideStatusMessage(RideStatusMessage rideStatusMessage) {
        RideStatus newRideStatus = rideStatusMessage.getRideStatus();

        switch (newRideStatus) {
            case ONGOING:
                if (rideStatus == RideStatus.NEW) {
                    rideHandlerList.forEach(IRideHandler::onRideStart);
                } else if (rideStatus == RideStatus.PAUSED) {
                    rideHandlerList.forEach(IRideHandler::onRideResume);
                }
                break;

            case PAUSED:
                rideHandlerList.forEach(IRideHandler::onRidePause);
                break;

            case FINISHED:
                rideHandlerList.forEach(IRideHandler::onRideEnd);
                break;
        }

        rideStatus = newRideStatus;
    }


}
