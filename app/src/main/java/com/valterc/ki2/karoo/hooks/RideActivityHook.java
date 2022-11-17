package com.valterc.ki2.karoo.hooks;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.valterc.ki2.utils.ActivityUtils;
import com.valterc.ki2.utils.ProcessUtils;

@SuppressLint("LogNotTimber")
public final class RideActivityHook {

    private RideActivityHook() {
    }

    public static boolean isRideActivityProcess() {
        return "io.hammerhead.rideapp:io.hammerhead.rideapp.rideActivityProcess".equals(ProcessUtils.getProcessName());
    }

    public static void preload(Context context) {
        Intent intentRideActivity = new Intent(Intent.ACTION_MAIN);
        intentRideActivity.setClassName("io.hammerhead.rideapp", "io.hammerhead.rideapp.views.ride.RideActivity");
        intentRideActivity.putExtra("ki2.preload", true);
        context.startActivity(intentRideActivity);
    }

    public static void tryHandlePreload(Context context) {
        if (RideActivityHook.isRideActivityProcess()) {
            Activity activity = ActivityUtils.getRunningActivity();
            if (activity != null) {
                boolean preload = activity.getIntent().getBooleanExtra("ki2.preload", false);
                Log.d("KI2", "Ride activity preload extra: " + preload);
                if (preload) {
                    Log.d("KI2", "Finish activity and broadcast events");
                    activity.finishAndRemoveTask();
                    context.sendBroadcast(new Intent("io.hammerhead.hx.intent.action.RIDE_STOP"));
                    context.sendBroadcast(new Intent("io.hammerhead.action.RIDE_APP_NOT_RECORDING_EXITED"));
                }
            }
        }
    }

}
