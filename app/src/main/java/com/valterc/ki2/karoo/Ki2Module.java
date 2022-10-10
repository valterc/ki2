package com.valterc.ki2.karoo;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.valterc.ki2.karoo.datatypes.BatteryTextDataType;
import com.valterc.ki2.karoo.datatypes.GearsDrivetrainDataType;
import com.valterc.ki2.karoo.datatypes.GearsGearsDataType;
import com.valterc.ki2.karoo.datatypes.GearsTextDataType;
import com.valterc.ki2.karoo.datatypes.ShiftCountTextDataType;
import com.valterc.ki2.karoo.datatypes.ShiftModeTextDataType;

import java.util.Arrays;
import java.util.List;

import io.hammerhead.sdk.v0.Module;
import io.hammerhead.sdk.v0.ModuleFactoryI;
import io.hammerhead.sdk.v0.SdkContext;
import io.hammerhead.sdk.v0.card.PostRideCard;
import io.hammerhead.sdk.v0.card.RideDetailsI;
import io.hammerhead.sdk.v0.datatype.SdkDataType;

public class Ki2Module extends Module {

    public static ModuleFactoryI factory = Ki2Module::new;

    private static final String NAME = "Ki2";
    private static final String VERSION = "0.1";

    private final Ki2ServiceClient serviceClient;
    private final Ki2Context ki2Context;

    public Ki2Module(@NonNull SdkContext context) {
        super(context);
        serviceClient = new Ki2ServiceClient(context);
        ki2Context = new Ki2Context(context, serviceClient);
    }

    @NonNull
    @Override
    public String getName() {
        return NAME;
    }

    @NonNull
    @Override
    public String getVersion() {
        return VERSION;
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
        ki2Context.setRideStatus(RideStatus.ONGOING);
        return super.onStart();
    }

    @Override
    public boolean onPause() {
        ki2Context.setRideStatus(RideStatus.PAUSED);
        return super.onPause();
    }

    @Override
    public boolean onResume() {
        ki2Context.setRideStatus(RideStatus.ONGOING);
        return super.onResume();
    }

    @Override
    public boolean onEnd() {
        ki2Context.setRideStatus(RideStatus.FINISHED);
        return super.onEnd();
    }
}
