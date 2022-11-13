package com.valterc.ki2.utils;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Method;

@SuppressLint("LogNotTimber")
public final class ProcessUtils {

    private ProcessUtils() {
    }

    @SuppressLint({"PrivateApi, DiscouragedPrivateApi", "LogNotTimber"})
    public static String getProcessName() {
        if (Build.VERSION.SDK_INT >= 28) {
            return Application.getProcessName();
        }

        try {
            Class<?> classActivityThread = Class.forName("android.app.ActivityThread");
            Method methodGetProcessName = classActivityThread.getDeclaredMethod("currentProcessName");
            return (String) methodGetProcessName.invoke(null);
        } catch (Exception e) {
            Log.e("KI2", "Unable to get activity: " + e);
            return null;
        }
    }

}
