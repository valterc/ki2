package com.valterc.ki2.karoo.datatypes;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.valterc.ki2.R;
import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.karoo.views.BatterySdkView;
import com.valterc.ki2.utils.function.FunctionUtils;

import java.util.Arrays;
import java.util.List;

import io.hammerhead.sdk.v0.datatype.view.SdkView;

public class BatteryGraphicalDataType extends GraphicalKi2DataType {

    private static final String TYPE_ID = "ki2::battery-graphical";

    private final List<Drawable> drawables;

    public BatteryGraphicalDataType(@NonNull Ki2Context context) {
        super(context);

        this.drawables = FunctionUtils.safeInvoke(() -> Arrays.asList(
                ContextCompat.getDrawable(getContext(), R.drawable.ic_hh_battery), ContextCompat.getDrawable(getContext(), R.drawable.ic_hh_gear)));
    }

    @NonNull
    @Override
    public String getDescription() {
        return "Shifting battery %.";
    }

    @NonNull
    @Override
    public String getDisplayName() {
        return "Battery";
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
        return new BatterySdkView(getKi2Context());
    }

}
