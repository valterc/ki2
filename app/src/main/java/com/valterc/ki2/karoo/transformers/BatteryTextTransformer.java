package com.valterc.ki2.karoo.transformers;

import androidx.annotation.NonNull;

import java.util.Map;

import io.hammerhead.sdk.v0.SdkContext;
import io.hammerhead.sdk.v0.datatype.Dependency;
import io.hammerhead.sdk.v0.datatype.transformer.SdkTransformer;
import timber.log.Timber;

public class BatteryTextTransformer extends SdkTransformer {

    public BatteryTextTransformer(@NonNull SdkContext context) {
        super(context);
    }

    @Override
    public double onDependencyChange(long timestamp, @NonNull Map<Dependency, Double> dependencies) {
        return Math.random() * 100;
    }

}
