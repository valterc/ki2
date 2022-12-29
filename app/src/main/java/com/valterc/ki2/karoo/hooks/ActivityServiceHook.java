package com.valterc.ki2.karoo.hooks;

import android.annotation.SuppressLint;

import kotlin.Lazy;
import kotlin.LazyKt;

@SuppressLint("LogNotTimber")
public class ActivityServiceHook {

    private ActivityServiceHook() {
    }

    private static final Lazy<Boolean> IN_ACTIVITY_SERVICE =
            LazyKt.lazy(() -> isInActivityService_1() || isInActivityService_2());

    private static boolean isInActivityService_1(){
        try {
            Class.forName("io.hammerhead.activityservice.ActivityServiceApplication");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isInActivityService_2(){
        try {
            Class.forName("io.hammerhead.activityservice.service.ActivityService");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Indicates if the running code is inside the Activity service application.
     *
     * @return True if the running process is the Activity service application, False otherwise.
     */
    public static boolean isInActivityService() {
        return IN_ACTIVITY_SERVICE.getValue();
    }

}
