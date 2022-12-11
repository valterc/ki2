package com.valterc.ki2.karoo.hooks;

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.valterc.ki2.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import io.hammerhead.sdk.v0.SdkContext;
import kotlin.Lazy;
import kotlin.LazyKt;

@SuppressLint("LogNotTimber")
public class KarooActivityServiceHook {

    private KarooActivityServiceHook() {
    }

    private static final Lazy<Boolean> IN_ACTIVITY_SERVICE =
            LazyKt.lazy(() -> isInActivityService_1() || isInActivityService_2());

    public static boolean isInActivityService() {
        return IN_ACTIVITY_SERVICE.getValue();
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
