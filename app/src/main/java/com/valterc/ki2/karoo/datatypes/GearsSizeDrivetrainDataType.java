package com.valterc.ki2.karoo.datatypes;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.valterc.ki2.R;
import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.karoo.views.DrivetrainSizeSdkView;
import com.valterc.ki2.utils.CallerUtils;

import java.util.Collections;
import java.util.List;

import io.hammerhead.sdk.v0.datatype.formatter.BuiltInFormatter;
import io.hammerhead.sdk.v0.datatype.formatter.SdkFormatter;
import io.hammerhead.sdk.v0.datatype.transformer.BuiltInTransformer;
import io.hammerhead.sdk.v0.datatype.transformer.SdkTransformer;
import io.hammerhead.sdk.v0.datatype.view.SdkView;

public class GearsSizeDrivetrainDataType extends Ki2DataType {

    private static final String TYPE_ID = "ki2::gears-size-drivetrain";

    private final List<Drawable> drawables;

    public GearsSizeDrivetrainDataType(@NonNull Ki2Context context) {
        super(context);

        this.drawables = CallerUtils.safeWrap(() -> Collections.singletonList(
                AppCompatResources.getDrawable(getContext(), R.drawable.ic_hh_gear)));
    }

    @NonNull
    @Override
    public String getDescription() {
        return "Drivetrain view with gear size.";
    }

    @NonNull
    @Override
    public String getDisplayName() {
        return "Drivetrain";
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
        return new BuiltInTransformer.Identity(getContext());
    }

    @NonNull
    @Override
    public SdkFormatter newFormatter() {
        return new BuiltInFormatter.None();
    }

    @NonNull
    @Override
    public SdkView newView() {
        return getKi2Context()
                .getInstanceManager()
                .getOrComputeInstance(DrivetrainSizeSdkView.class.getSimpleName(), SdkView.class, () -> new DrivetrainSizeSdkView(getKi2Context()));
    }
}
