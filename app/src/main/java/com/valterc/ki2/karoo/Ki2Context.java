package com.valterc.ki2.karoo;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.valterc.ki2.data.message.RideStatusMessage;
import com.valterc.ki2.data.ride.RideStatus;
import com.valterc.ki2.karoo.audio.IAudioAlertManager;
import com.valterc.ki2.karoo.audio.LocalAudioAlertManager;
import com.valterc.ki2.karoo.audio.RemoteAudioAlertManager;
import com.valterc.ki2.karoo.hooks.ActivityServiceHook;
import com.valterc.ki2.karoo.instance.InstanceManager;
import com.valterc.ki2.karoo.service.ServiceClient;

import java.util.function.Consumer;

import io.hammerhead.sdk.v0.SdkContext;

@SuppressWarnings("FieldCanBeLocal")
@SuppressLint("LogNotTimber")
public class Ki2Context {

    private final Handler handler;
    private final InstanceManager instanceManager;
    private final SdkContext sdkContext;
    private final ServiceClient serviceClient;
    private final IAudioAlertManager audioAlertManager;
    private RideStatus rideStatus;

    private final Consumer<RideStatusMessage> onRideStatusMessage = this::onRideStatusMessage;

    public Ki2Context(SdkContext sdkContext) {
        this.sdkContext = sdkContext;
        this.rideStatus = RideStatus.NEW;
        this.handler = new Handler(Looper.getMainLooper());
        this.instanceManager = new InstanceManager();
        this.serviceClient = new ServiceClient(this);
        this.audioAlertManager = ActivityServiceHook.isInActivityService() ? new LocalAudioAlertManager(this) : new RemoteAudioAlertManager(this);

        this.serviceClient.getCustomMessageClient().registerRideStatusWeakListener(onRideStatusMessage);
    }

    private void onRideStatusMessage(RideStatusMessage rideStatusMessage) {
        rideStatus = rideStatusMessage.getRideStatus();
        Log.d("KI2", "Updated ride status: " + rideStatus);
    }

    public Handler getHandler() {
        return handler;
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

    public IAudioAlertManager getAudioAlertManager() {
        return audioAlertManager;
    }

    public RideStatus getRideStatus() {
        return rideStatus;
    }

}
