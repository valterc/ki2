package com.valterc.ki2.karoo;

import android.annotation.SuppressLint;
import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garmin.fit.DateTime;
import com.garmin.fit.Decode;
import com.garmin.fit.DeveloperDataIdMesg;
import com.garmin.fit.DeveloperFieldDescription;
import com.garmin.fit.DeveloperFieldDescriptionListener;
import com.garmin.fit.Event;
import com.garmin.fit.EventMesg;
import com.garmin.fit.EventType;
import com.garmin.fit.FileEncoder;
import com.garmin.fit.FitDecoder;
import com.garmin.fit.Mesg;
import com.garmin.fit.MesgDefinition;
import com.garmin.fit.MesgDefinitionListener;
import com.garmin.fit.MesgListener;
import com.valterc.ki2.BuildConfig;
import com.valterc.ki2.data.message.RideStatusMessage;
import com.valterc.ki2.data.ride.RideStatus;
import com.valterc.ki2.karoo.battery.LowBatteryHandler;
import com.valterc.ki2.karoo.datatypes.BatteryGraphicalDataType;
import com.valterc.ki2.karoo.datatypes.BatteryTextDataType;
import com.valterc.ki2.karoo.datatypes.DeviceNameTextDataType;
import com.valterc.ki2.karoo.datatypes.FrontGearSizeTextDataType;
import com.valterc.ki2.karoo.datatypes.FrontGearTextDataType;
import com.valterc.ki2.karoo.datatypes.FrontShiftCountTextDataType;
import com.valterc.ki2.karoo.datatypes.GearRatioTextDataType;
import com.valterc.ki2.karoo.datatypes.GearsDrivetrainDataType;
import com.valterc.ki2.karoo.datatypes.GearsGearsDataType;
import com.valterc.ki2.karoo.datatypes.GearsSizeDrivetrainDataType;
import com.valterc.ki2.karoo.datatypes.GearsSizeGearsDataType;
import com.valterc.ki2.karoo.datatypes.GearsSizeTextDataType;
import com.valterc.ki2.karoo.datatypes.GearsTextDataType;
import com.valterc.ki2.karoo.datatypes.RearGearSizeTextDataType;
import com.valterc.ki2.karoo.datatypes.RearGearTextDataType;
import com.valterc.ki2.karoo.datatypes.RearShiftCountTextDataType;
import com.valterc.ki2.karoo.datatypes.ShiftCountTextDataType;
import com.valterc.ki2.karoo.datatypes.ShiftModeDataType;
import com.valterc.ki2.karoo.datatypes.ShiftModeTextDataType;
import com.valterc.ki2.karoo.handlers.HandlerManager;
import com.valterc.ki2.karoo.hooks.ActivityServiceHook;
import com.valterc.ki2.karoo.hooks.DataSyncServiceHook;
import com.valterc.ki2.karoo.hooks.RideActivityHook;
import com.valterc.ki2.karoo.overlay.OverlayManager;
import com.valterc.ki2.karoo.shifting.ShiftingAudioAlertHandler;
import com.valterc.ki2.karoo.update.UpdateAvailableHandler;
import com.valterc.ki2.karoo.update.UpdateAvailableNotification;
import com.valterc.ki2.utils.ProcessUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.hammerhead.sdk.v0.Module;
import io.hammerhead.sdk.v0.ModuleFactoryI;
import io.hammerhead.sdk.v0.SdkContext;
import io.hammerhead.sdk.v0.card.FitFileListener;
import io.hammerhead.sdk.v0.card.PostRideCard;
import io.hammerhead.sdk.v0.card.RideDetailsI;
import io.hammerhead.sdk.v0.datatype.SdkDataType;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
@SuppressLint("LogNotTimber")
public class Ki2Module extends Module {

    public static final ModuleFactoryI factory = Ki2Module::new;

    private static final String NAME = "Ki2";

    private final Ki2Context ki2Context;
    private final HandlerManager handlerManager;

    public Ki2Module(@NonNull SdkContext context) {
        super(context);

        RideActivityHook.tryHandlePreload(context);
        ki2Context = new Ki2Context(context);
        UpdateAvailableNotification.clearUpdateAvailableNotification(context);

        if (ActivityServiceHook.isInActivityService()) {
            handlerManager = new HandlerManager(ki2Context, Arrays.asList(
                    new UpdateAvailableHandler(ki2Context),
                    new LowBatteryHandler(ki2Context),
                    new ShiftingAudioAlertHandler(ki2Context)));
        } else if (RideActivityHook.isRideActivityProcess()) {
            handlerManager = new HandlerManager(ki2Context, Collections.singletonList(new OverlayManager(ki2Context)));
        } else {
            handlerManager = null;
        }


        if (DataSyncServiceHook.isInDataSyncService()) {
            ki2Context.getHandler().postDelayed(() -> DataSyncServiceHook.init(context), 15_000);
            ki2Context.getHandler().postDelayed(() -> DataSyncServiceHook.init(context), 20_000);
            ki2Context.getHandler().postDelayed(() -> DataSyncServiceHook.init(context), 25_000);
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
                new DeviceNameTextDataType(ki2Context),
                new BatteryTextDataType(ki2Context),
                new GearsTextDataType(ki2Context),
                new GearsSizeTextDataType(ki2Context),
                new GearRatioTextDataType(ki2Context),
                new FrontGearTextDataType(ki2Context),
                new FrontGearSizeTextDataType(ki2Context),
                new RearGearTextDataType(ki2Context),
                new RearGearSizeTextDataType(ki2Context),
                new ShiftModeTextDataType(ki2Context),
                new ShiftCountTextDataType(ki2Context),
                new FrontShiftCountTextDataType(ki2Context),
                new RearShiftCountTextDataType(ki2Context),
                new GearsDrivetrainDataType(ki2Context),
                new GearsSizeDrivetrainDataType(ki2Context),
                new GearsGearsDataType(ki2Context),
                new GearsSizeGearsDataType(ki2Context),
                new ShiftModeDataType(ki2Context),
                new BatteryGraphicalDataType(ki2Context));
    }

    @Nullable
    @Override
    public PostRideCard postRideCard(@NonNull RideDetailsI details) {
        Log.i("KI2", "Ride name:" + details.getName());
        Log.i("KI2", "Id:" + details.getId());
        Log.i("KI2", "Process:" + ProcessUtils.getProcessName());

        /*

        FileEncoder fileEncoder = new FileEncoder();
        EventMesg eventMesg = new EventMesg();
        eventMesg.setLocalNum(4);
        eventMesg.setTimestamp(new DateTime(0));
        eventMesg.setEvent(Event.FRONT_GEAR_CHANGE); // eventMesg.setEvent(Event.REAR_GEAR_CHANGE);
        eventMesg.setEventType(EventType.MARKER);
        fileEncoder.write(a);
        */
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
