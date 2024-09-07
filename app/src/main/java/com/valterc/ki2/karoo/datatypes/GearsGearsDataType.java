package com.valterc.ki2.karoo.datatypes;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.valterc.ki2.R;
import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.karoo.views.GearsSdkView;
import com.valterc.ki2.utils.function.FunctionUtils;

import java.util.Collections;
import java.util.List;

import io.hammerhead.sdk.v0.datatype.view.SdkView;

public class GearsGearsDataType extends GraphicalKi2DataType {

    private static final String TYPE_ID = "ki2::gears-gears";

    private final List<Drawable> drawables;

    public GearsGearsDataType(@NonNull Ki2Context context) {
        super(context);

        this.drawables = FunctionUtils.safeInvoke(() -> Collections.singletonList(
                ContextCompat.getDrawable(getContext(), R.drawable.ic_hh_gear)));
    }

    @NonNull
    @Override
    public String getDescription() {
        return "Gears view with gear index.";
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

    @NonNull
    @Override
    public SdkView newView() {
        return new GearsSdkView(getKi2Context());
    }
}
