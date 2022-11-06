package com.valterc.ki2.karoo;

import android.util.Log;

import com.valterc.ki2.data.message.Message;
import com.valterc.ki2.data.message.MessageType;
import com.valterc.ki2.data.message.RideStatusMessage;
import com.valterc.ki2.data.ride.RideStatus;
import com.valterc.ki2.karoo.instance.InstanceManager;
import com.valterc.ki2.karoo.service.Ki2ServiceClient;

import java.util.function.Consumer;

import io.hammerhead.sdk.v0.SdkContext;

public class Ki2Context {

    private final InstanceManager instanceManager;
    private final SdkContext sdkContext;
    private final Ki2ServiceClient serviceClient;

    private RideStatus rideStatus;

    @SuppressWarnings("FieldCanBeLocal")
    private final Consumer<Message> messageConsumer = (message) -> {
        if (message.getMessageType() == MessageType.RIDE_STATUS) {
            RideStatusMessage rideStatusMessage = RideStatusMessage.parse(message);
            if (rideStatusMessage != null) {
                rideStatus = rideStatusMessage.getRideStatus();
                Log.d("KI2", "Updated ride status: " + rideStatus);
            }
        }
    };

    public Ki2Context(SdkContext sdkContext, Ki2ServiceClient serviceClient) {
        this.sdkContext = sdkContext;
        this.serviceClient = serviceClient;
        this.instanceManager = new InstanceManager();
        this.rideStatus = RideStatus.NEW;

        serviceClient.registerMessageWeakListener(messageConsumer);
    }

    public SdkContext getSdkContext() {
        return sdkContext;
    }

    public Ki2ServiceClient getServiceClient() {
        return serviceClient;
    }

    public InstanceManager getInstanceManager() {
        return instanceManager;
    }

    public RideStatus getRideStatus() {
        return rideStatus;
    }

}
