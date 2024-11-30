package com.valterc.ki2.karoo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.valterc.ki2.data.message.RideStatusMessage;
import com.valterc.ki2.data.ride.RideStatus;
import com.valterc.ki2.karoo.audio.IAudioAlertManager;
import com.valterc.ki2.karoo.hooks.ActivityServiceHook;
import com.valterc.ki2.karoo.instance.InstanceManager;
import com.valterc.ki2.karoo.screen.ScreenHelper;
import com.valterc.ki2.karoo.service.ServiceClient;
import com.valterc.ki2.utils.SafeHandler;

import java.util.function.Consumer;

import io.hammerhead.karooext.KarooSystemService;
import io.hammerhead.sdk.v0.SdkContext;

@SuppressWarnings("FieldCanBeLocal")
@SuppressLint("LogNotTimber")
@Deprecated
public class Ki2Context {

    private final Handler handler;
    private final InstanceManager instanceManager;
    private final Context context;
    private final KarooSystemService karooSystem;
    private final ServiceClient serviceClient;
    private final IAudioAlertManager audioAlertManager;
    private final ScreenHelper screenHelper;
    private final boolean fullyInitialized;

    private RideStatus rideStatus;

    private final Consumer<RideStatusMessage> onRideStatusMessage = this::onRideStatusMessage;

    public Ki2Context(Context context) {
        this.karooSystem = new KarooSystemService(context);
        this.context = context;
        this.rideStatus = RideStatus.NEW;
        this.handler = new SafeHandler(Looper.getMainLooper());
        this.instanceManager = new InstanceManager();
        this.serviceClient = new ServiceClient(context);
        this.audioAlertManager = null;
        this.screenHelper = new ScreenHelper(this);

        this.serviceClient.getCustomMessageClient().registerRideStatusWeakListener(onRideStatusMessage);
        this.fullyInitialized = true;
    }

    public SdkContext getSdkContext(){
        return null;
    }

    private void onRideStatusMessage(RideStatusMessage rideStatusMessage) {
        rideStatus = rideStatusMessage.getRideStatus();
    }

    public void whenFullyInitialized(Runnable runnable) {
        if (fullyInitialized) {
            runnable.run();
        } else {
            handler.postDelayed(() -> whenFullyInitialized(runnable), 250);
        }
    }

    public Handler getHandler() {
        return handler;
    }

    public Context getContext() {
        return context;
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

    public ScreenHelper getScreenHelper() {
        return screenHelper;
    }

    public RideStatus getRideStatus() {
        return rideStatus;
    }

}
