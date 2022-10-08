package com.valterc.ki2.karoo.datatypes;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.valterc.ki2.R;
import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.karoo.formatters.GearsTextFormatter;

import java.util.Collections;
import java.util.List;

import io.hammerhead.sdk.v0.datatype.formatter.SdkFormatter;
import io.hammerhead.sdk.v0.datatype.transformer.BuiltInTransformer;
import io.hammerhead.sdk.v0.datatype.transformer.SdkTransformer;
import io.hammerhead.sdk.v0.datatype.view.BuiltInView;
import io.hammerhead.sdk.v0.datatype.view.SdkView;

public class GearsTextDataType extends Ki2DataType {

    private static final String TYPE_ID = "ki2-gears-text";

    private final List<Drawable> drawables;

    public GearsTextDataType(@NonNull Ki2Context context) {
        super(context);

        this.drawables = Collections.singletonList(
                AppCompatResources.getDrawable(getContext(), R.drawable.ic_gear));
    }

    @NonNull
    @Override
    public String getDescription() {
        return "Shifting gears in format FF-RR (FF Front, RR Rear), example: 01-05 for gear 1 in front and gear 5 in rear.";
    }

    @NonNull
    @Override
    public String getDisplayName() {
        return "Gears";
    }

    @NonNull
    @Override
    public String getTypeId() {
        return TYPE_ID;
    }


    @Nullable
    @Override
    public List<Drawable> displayIcons() {
        return drawables;
    }

    @Override
    public double getSampleValue() {
        return 01.07;
    }

    @NonNull
    @Override
    public SdkTransformer newTransformer() {
        return getKi2Context()
                .getInstanceManager()
                .getOrComputeInstance(BuiltInTransformer.Identity.class.getSimpleName(), SdkTransformer.class, () -> new BuiltInTransformer.Identity(getContext()));
    }

    @NonNull
    @Override
    public SdkFormatter newFormatter() {
        return getKi2Context()
                .getInstanceManager()
                .getOrComputeInstance(GearsTextFormatter.class.getSimpleName(), SdkFormatter.class, () -> new GearsTextFormatter(getKi2Context()));
    }

    @NonNull
    @Override
    public SdkView newView() {
        return getKi2Context()
                .getInstanceManager()
                .getOrComputeInstance(BuiltInView.Numeric.class.getSimpleName(), SdkView.class, () -> new BuiltInView.Numeric(getContext()));
    }
}
