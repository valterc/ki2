package com.valterc.ki2.karoo;

import com.valterc.ki2.karoo.instance.InstanceManager;

import io.hammerhead.sdk.v0.SdkContext;

public class Ki2Context {

    private final InstanceManager instanceManager;
    private final SdkContext sdkContext;
    private final Ki2ServiceClient serviceClient;
    private RideStatus rideStatus;

    public Ki2Context(SdkContext sdkContext, Ki2ServiceClient serviceClient) {
        this.sdkContext = sdkContext;
        this.serviceClient = serviceClient;
        this.instanceManager = new InstanceManager();
        this.rideStatus = RideStatus.NEW;
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

    public void setRideStatus(RideStatus rideStatus) {
        this.rideStatus = rideStatus;
    }
}
