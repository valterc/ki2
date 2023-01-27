package com.valterc.ki2.update;

import android.content.Context;

import com.valterc.ki2.data.device.DeviceStore;

public class PostUpdateContext {

    private final Context context;
    private final DeviceStore deviceStore;

    public PostUpdateContext(Context context, DeviceStore deviceStore) {
        this.context = context;
        this.deviceStore = deviceStore;
    }

    public Context getContext() {
        return context;
    }

    public DeviceStore getDeviceStore() {
        return deviceStore;
    }
}
