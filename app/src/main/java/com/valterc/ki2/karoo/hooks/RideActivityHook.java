package com.valterc.ki2.karoo.hooks;

import com.valterc.ki2.utils.ProcessUtils;

public final class RideActivityHook {

    private RideActivityHook() {
    }

    public static boolean isRideActivityProcess() {
        return "io.hammerhead.rideapp:io.hammerhead.rideapp.rideActivityProcess".equals(ProcessUtils.getProcessName());
    }

}
