package com.valterc.ki2.karoo.datatypes;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.valterc.ki2.R;
import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.karoo.formatters.RearShiftCountTextFormatter;
import com.valterc.ki2.utils.function.FunctionUtils;

import java.util.Collections;
import java.util.List;

import io.hammerhead.sdk.v0.datatype.formatter.SdkFormatter;
import io.hammerhead.sdk.v0.datatype.transformer.BuiltInTransformer;
import io.hammerhead.sdk.v0.datatype.transformer.SdkTransformer;
import io.hammerhead.sdk.v0.datatype.view.BuiltInView;
import io.hammerhead.sdk.v0.datatype.view.SdkView;

public class RearShiftCountTextDataType extends Ki2DataType {

    private static final String TYPE_ID = "ki2::rear-shiftcount-text";

    private final List<Drawable> drawables;

    public RearShiftCountTextDataType(@NonNull Ki2Context context) {
        super(context);

        this.drawables = FunctionUtils.safeInvoke(() -> Collections.singletonList(
                ContextCompat.getDrawable(getContext(), R.drawable.ic_hh_gear)));
    }

    @NonNull
    @Override
    public String getDescription() {
        return "Total number of rear shifts during the ride.";
    }

    @NonNull
    @Override
    public String getDisplayName() {
        return "R Shf Cnt";
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
                .getOrComputeInstance(RearShiftCountTextFormatter.class.getSimpleName(), SdkFormatter.class, () -> new RearShiftCountTextFormatter(getKi2Context()));
    }

    @NonNull
    @Override
    public SdkView newView() {
        return getKi2Context()
                .getInstanceManager()
                .getOrComputeInstance(BuiltInView.Numeric.class.getSimpleName(), SdkView.class, () -> new BuiltInView.Numeric(getContext()));
    }
}