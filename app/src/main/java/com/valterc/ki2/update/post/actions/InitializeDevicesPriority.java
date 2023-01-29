package com.valterc.ki2.update.post.actions;

import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.preferences.device.DevicePreferences;
import com.valterc.ki2.update.PostUpdateContext;

import java.util.Comparator;
import java.util.stream.Collectors;

public class InitializeDevicesPriority implements IPostUpdateAction {

    @Override
    public void execute(PostUpdateContext context) {
        int priority = 0;
        for (DeviceId deviceId : context.getDeviceStore().getDevices().stream().sorted(Comparator.comparing(DeviceId::getDeviceNumber)).collect(Collectors.toList())) {
            new DevicePreferences(context.getContext(), deviceId).setPriority(priority++);
        }
    }

}
