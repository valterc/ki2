package com.valterc.ki2.karoo.screen;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.PowerManager;

import com.valterc.ki2.karoo.Ki2Context;

@SuppressLint("LogNotTimber")
@SuppressWarnings("FieldCanBeLocal")
public class ScreenHelper {

    private final static int TIME_MS_ACQUIRE_LOCK_TIMEOUT = 1000;

    private final Ki2Context ki2Context;

    public ScreenHelper(Ki2Context ki2Context) {
        this.ki2Context = ki2Context;
    }

    @SuppressWarnings("deprecation")
    public void turnScreenOn() {
        PowerManager powerManager = (PowerManager) ki2Context.getSdkContext().getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE,
                "Ki2::WakeLock");

        wakeLock.acquire(TIME_MS_ACQUIRE_LOCK_TIMEOUT);
        wakeLock.release();
    }

}
