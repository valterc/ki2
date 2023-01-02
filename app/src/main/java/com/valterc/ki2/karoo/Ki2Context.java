package com.valterc.ki2.karoo;

import android.annotation.SuppressLint;
import android.util.Log;

import com.valterc.ki2.data.message.RideStatusMessage;
import com.valterc.ki2.data.ride.RideStatus;
import com.valterc.ki2.karoo.instance.InstanceManager;
import com.valterc.ki2.karoo.service.ServiceClient;

import java.util.function.Consumer;

import io.hammerhead.sdk.v0.SdkContext;

@SuppressWarnings("FieldCanBeLocal")
@SuppressLint("LogNotTimber")
public class Ki2Context {

    private final InstanceManager instanceManager;
    private final SdkContext sdkContext;
    private final ServiceClient serviceClient;
    private RideStatus rideStatus;

    private final Consumer<RideStatusMessage> onRideStatusMessage = this::onRideStatusMessage;

    public Ki2Context(SdkContext sdkContext, ServiceClient serviceClient) {
        this.sdkContext = sdkContext;
        this.serviceClient = serviceClient;
        this.instanceManager = new InstanceManager();
        this.rideStatus = RideStatus.NEW;

        serviceClient.getCustomMessageClient().registerRideStatusWeakListener(onRideStatusMessage);
    }

    private void onRideStatusMessage(RideStatusMessage rideStatusMessage) {
        rideStatus = rideStatusMessage.getRideStatus();
        Log.d("KI2", "Updated ride status: " + rideStatus);
    }

    public SdkContext getSdkContext() {
        return sdkContext;
    }

    public ServiceClient getServiceClient() {
        return serviceClient;
    }

    public InstanceManager getInstanceManager() {
        return instanceManager;
    }

    public RideStatus getRideStatus() {
        return rideStatus;
    }

}
