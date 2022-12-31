package com.valterc.ki2.karoo;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.valterc.ki2.BuildConfig;
import com.valterc.ki2.data.message.RideStatusMessage;
import com.valterc.ki2.data.ride.RideStatus;
import com.valterc.ki2.karoo.battery.LowBatteryHandler;
import com.valterc.ki2.karoo.datatypes.BatteryTextDataType;
import com.valterc.ki2.karoo.datatypes.GearsDrivetrainDataType;
import com.valterc.ki2.karoo.datatypes.GearsGearsDataType;
import com.valterc.ki2.karoo.datatypes.GearsTextDataType;
import com.valterc.ki2.karoo.datatypes.ShiftCountTextDataType;
import com.valterc.ki2.karoo.datatypes.ShiftModeTextDataType;
import com.valterc.ki2.karoo.handlers.HandlerManager;
import com.valterc.ki2.karoo.hooks.ActivityServiceHook;
import com.valterc.ki2.karoo.hooks.RideActivityHook;
import com.valterc.ki2.karoo.service.ServiceClient;
import com.valterc.ki2.karoo.shifting.ShiftingAudioAlertHandler;
import com.valterc.ki2.karoo.update.UpdateAvailableHandler;
import com.valterc.ki2.karoo.update.UpdateAvailableNotification;

import java.util.Arrays;
import java.util.List;

import io.hammerhead.sdk.v0.Module;
import io.hammerhead.sdk.v0.ModuleFactoryI;
import io.hammerhead.sdk.v0.SdkContext;
import io.hammerhead.sdk.v0.card.PostRideCard;
import io.hammerhead.sdk.v0.card.RideDetailsI;
import io.hammerhead.sdk.v0.datatype.SdkDataType;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
@SuppressLint("LogNotTimber")
public class Ki2Module extends Module {

    public static ModuleFactoryI factory = Ki2Module::new;

    private static final String NAME = "Ki2";

    private final ServiceClient serviceClient;
    private final Ki2Context ki2Context;
    private final HandlerManager handlerManager;

    public Ki2Module(@NonNull SdkContext context) {
        super(context);

        RideActivityHook.tryHandlePreload(context);
        serviceClient = new ServiceClient(context);
        ki2Context = new Ki2Context(context, serviceClient);
        UpdateAvailableNotification.clearUpdateAvailableNotification(context);

        if (ActivityServiceHook.isInActivityService()) {
            handlerManager = new HandlerManager(serviceClient, Arrays.asList(
                    new UpdateAvailableHandler(ki2Context),
                    new LowBatteryHandler(ki2Context),
                    new ShiftingAudioAlertHandler(ki2Context)));
        } else {
            handlerManager = null;
        }
    }

    @NonNull
    @Override
    public String getName() {
        return NAME;
    }

    @NonNull
    @Override
    public String getVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @NonNull
    @Override
    protected List<SdkDataType> provideDataTypes() {
        return Arrays.asList(
                new BatteryTextDataType(ki2Context),
                new GearsTextDataType(ki2Context),
                new ShiftModeTextDataType(ki2Context),
                new ShiftCountTextDataType(ki2Context),
                new GearsDrivetrainDataType(ki2Context),
                new GearsGearsDataType(ki2Context));
    }

    @Nullable
    @Override
    public PostRideCard postRideCard(@NonNull RideDetailsI details) {
        return super.postRideCard(details);
    }

    @Override
    public boolean onStart() {
        ki2Context.getServiceClient().sendMessage(new RideStatusMessage(RideStatus.ONGOING));
        return super.onStart();
    }

    @Override
    public boolean onPause() {
        ki2Context.getServiceClient().sendMessage(new RideStatusMessage(RideStatus.PAUSED));
        return super.onPause();
    }

    @Override
    public boolean onResume() {
        ki2Context.getServiceClient().sendMessage(new RideStatusMessage(RideStatus.ONGOING));
        return super.onResume();
    }

    @Override
    public boolean onEnd() {
        ki2Context.getServiceClient().sendMessage(new RideStatusMessage(RideStatus.FINISHED));
        return super.onEnd();
    }

}
