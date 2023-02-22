package com.valterc.ki2.update.post;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.valterc.ki2.data.device.DeviceStore;

public class PostUpdateContext {

    private final Context context;
    private final DeviceStore deviceStore;

    public PostUpdateContext(@NonNull Context context, @Nullable DeviceStore deviceStore) {
        this.context = context;
        this.deviceStore = deviceStore;
    }

    @NonNull
    public Context getContext() {
        return context;
    }

    @Nullable
    public DeviceStore getDeviceStore() {
        return deviceStore;
    }
}
