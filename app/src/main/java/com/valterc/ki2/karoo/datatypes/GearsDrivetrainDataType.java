package com.valterc.ki2.karoo.datatypes;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.valterc.ki2.R;
import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.karoo.views.DrivetrainSdkView;
import com.valterc.ki2.utils.function.FunctionUtils;

import java.util.Collections;
import java.util.List;

import io.hammerhead.sdk.v0.datatype.view.SdkView;

public class GearsDrivetrainDataType extends GraphicalKi2DataType {

    private static final String TYPE_ID = "ki2::gears-drivetrain";

    private final List<Drawable> drawables;

    public GearsDrivetrainDataType(@NonNull Ki2Context context) {
        super(context);

        this.drawables = FunctionUtils.safeInvoke(() -> Collections.singletonList(
                AppCompatResources.getDrawable(getContext(), R.drawable.ic_hh_gear)));
    }

    @NonNull
    @Override
    public String getDescription() {
        return "Drivetrain view with gear index.";
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
    public SdkView newView() {
        return new DrivetrainSdkView(getKi2Context());
    }
}
