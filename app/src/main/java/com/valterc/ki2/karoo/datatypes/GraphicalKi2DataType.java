package com.valterc.ki2.karoo.datatypes;

import androidx.annotation.NonNull;

import com.valterc.ki2.karoo.Ki2Context;

import io.hammerhead.sdk.v0.datatype.formatter.BuiltInFormatter;
import io.hammerhead.sdk.v0.datatype.formatter.SdkFormatter;
import io.hammerhead.sdk.v0.datatype.transformer.BuiltInTransformer;
import io.hammerhead.sdk.v0.datatype.transformer.SdkTransformer;

public abstract class GraphicalKi2DataType extends Ki2DataType {

    private final SdkTransformer transformer;
    private final SdkFormatter formatter;

    public GraphicalKi2DataType(@NonNull Ki2Context context) {
        super(context);

        transformer = new BuiltInTransformer.Identity(getContext());
        formatter = new BuiltInFormatter.None();
    }

    @NonNull
    @Override
    public SdkTransformer newTransformer() {
        return transformer;
    }

    @NonNull
    @Override
    public SdkFormatter newFormatter() {
        return formatter;
    }

}
