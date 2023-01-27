package com.valterc.ki2.karoo.datatypes;

import androidx.annotation.NonNull;

import com.valterc.ki2.karoo.Ki2Context;

import io.hammerhead.sdk.v0.datatype.formatter.BuiltInFormatter;
import io.hammerhead.sdk.v0.datatype.formatter.SdkFormatter;
import io.hammerhead.sdk.v0.datatype.transformer.BuiltInTransformer;
import io.hammerhead.sdk.v0.datatype.transformer.SdkTransformer;
import io.hammerhead.sdk.v0.datatype.view.SdkView;

public abstract class GraphicalKi2DataType extends Ki2DataType {

    private SdkView view;

    public GraphicalKi2DataType(@NonNull Ki2Context context) {
        super(context);
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
        if (view == null) {
            view = createView();
        }
        return view;
    }

    @NonNull
    public abstract SdkView createView();

}
