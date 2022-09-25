package com.valterc.ki2.karoo;

import androidx.annotation.NonNull;

import com.valterc.ki2.karoo.datatypes.BatteryTextDataType;

import java.util.Arrays;
import java.util.List;

import io.hammerhead.sdk.v0.Module;
import io.hammerhead.sdk.v0.ModuleFactoryI;
import io.hammerhead.sdk.v0.SdkContext;
import io.hammerhead.sdk.v0.datatype.SdkDataType;

public class Ki2Module extends Module {

    public static ModuleFactoryI factory = context -> new Ki2Module(context);

    private static final String NAME = "Ki2";
    private static final String VERSION = "0.1";

    public Ki2Module(@NonNull SdkContext context) {
        super(context);
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
        return Arrays.asList(new BatteryTextDataType(getContext()));
    }

}
