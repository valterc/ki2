package com.valterc.ki2.karoo.hooks;

import android.annotation.SuppressLint;

@SuppressLint("LogNotTimber")
public class KarooActivityServiceHook {

    private KarooActivityServiceHook() {
    }

    public static boolean isInActivityService() {
        return isInActivityService_1() || isInActivityService_2();
    }

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

}
