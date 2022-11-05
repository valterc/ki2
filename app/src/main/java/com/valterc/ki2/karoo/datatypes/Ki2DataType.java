package com.valterc.ki2.karoo.datatypes;

import androidx.annotation.NonNull;

import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.karoo.service.Ki2ServiceClient;

import io.hammerhead.sdk.v0.datatype.SdkDataType;

public abstract class Ki2DataType extends SdkDataType {

    private final Ki2Context context;

    public Ki2DataType(@NonNull Ki2Context context) {
        super(context.getSdkContext());
        this.context = context;
    }

    @NonNull
    public Ki2Context getKi2Context() {
        return context;
    }

    @NonNull
    public Ki2ServiceClient getServiceClient() {
        return context.getServiceClient();
    }

}
