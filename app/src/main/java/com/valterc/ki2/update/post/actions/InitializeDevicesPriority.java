package com.valterc.ki2.update.post.actions;

import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.device.DeviceStore;
import com.valterc.ki2.data.preferences.device.DevicePreferences;
import com.valterc.ki2.update.post.PostUpdateContext;

import java.util.Comparator;
import java.util.stream.Collectors;

import timber.log.Timber;

public class InitializeDevicesPriority implements IPostInitPostUpdateAction {

    @Override
    public void execute(PostUpdateContext context) {
        DeviceStore deviceStore = context.getDeviceStore();
        if (deviceStore == null) {
            Timber.w("Unable to initialize devices priority, device store is null");
            return;
        }

        int priority = 0;
        for (DeviceId deviceId : deviceStore.getDevices().stream().sorted(Comparator.comparing(DeviceId::getDeviceNumber)).collect(Collectors.toList())) {
            new DevicePreferences(context.getContext(), deviceId).setPriority(priority++);
        }
    }

}
