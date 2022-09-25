package com.valterc.ki2.karoo.datatypes;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.valterc.ki2.karoo.formatters.BatteryTextFormatter;
import com.valterc.ki2.karoo.transformers.BatteryTextTransformer;

import java.util.List;

import io.hammerhead.sdk.v0.SdkContext;
import io.hammerhead.sdk.v0.datatype.SdkDataType;
import io.hammerhead.sdk.v0.datatype.formatter.SdkFormatter;
import io.hammerhead.sdk.v0.datatype.transformer.SdkTransformer;
import io.hammerhead.sdk.v0.datatype.view.BuiltInView;
import io.hammerhead.sdk.v0.datatype.view.SdkView;

public class BatteryTextDataType extends SdkDataType {

    private static final String TYPE_ID = "ki2-battery-text";

    public BatteryTextDataType(@NonNull SdkContext context) {
        super(context);
    }

    @NonNull
    @Override
    public String getDescription() {
        return "Di2 shifting battery %.";
    }

    @NonNull
    @Override
    public String getDisplayName() {
        return "Shifting Battery %";
    }

    @NonNull
    @Override
    public String getTypeId() {
        return TYPE_ID;
    }

    @Nullable
    @Override
    public List<Drawable> displayIcons() {
        return super.displayIcons();
    }

    @Override
    public double getSampleValue() {
        return 80;
    }

    @NonNull
    @Override
    public SdkFormatter newFormatter() {
        return new BatteryTextFormatter();
    }

    @NonNull
    @Override
    public SdkTransformer newTransformer() {
        return new BatteryTextTransformer(getContext());
    }

    @NonNull
    @Override
    public SdkView newView() {
        return new BuiltInView.Numeric(getContext());
    }
}
