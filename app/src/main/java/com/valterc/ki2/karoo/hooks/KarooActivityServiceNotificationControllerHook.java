package com.valterc.ki2.karoo.hooks;

import android.annotation.SuppressLint;
import android.util.Log;

import com.valterc.ki2.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import io.hammerhead.sdk.v0.SdkContext;

@SuppressLint("LogNotTimber")
public class KarooActivityServiceNotificationControllerHook {

    private KarooActivityServiceNotificationControllerHook() {
    }

    public static boolean showSensorLowBatteryNotification(SdkContext context, String deviceName) {
        try {
            Class<?> classActivityServiceApplication = Class.forName("io.hammerhead.activityservice.ActivityServiceApplication");
            Method methodGetActivityComponent = classActivityServiceApplication.getMethod("getActivityComponent");
            Object activityComponent = methodGetActivityComponent.invoke(context.getBaseContext());

            assert activityComponent != null;
            Method methodNotificationController = activityComponent.getClass().getMethod("notificationController");
            Object notificationController = methodNotificationController.invoke(activityComponent);

            assert notificationController != null;
            Field fieldNotificationSubject = notificationController.getClass().getDeclaredField("notificationSubject");
            fieldNotificationSubject.setAccessible(true);
            Object notificationSubject = fieldNotificationSubject.get(notificationController);

            assert notificationSubject != null;
            Method[] allMethods = notificationSubject.getClass().getDeclaredMethods();

            for (Method method: allMethods) {
                Type[] types = method.getGenericParameterTypes();
                if (types.length == 1 && types[0].toString().equals("T")) {
                    method.invoke(notificationSubject, KarooNotificationHook.buildSensorLowBatteryNotification(context.getString(R.string.text_param_di2_name, deviceName)));
                    return true;
                }
            }

            throw new Exception("Unable to hook into notification publisher");
        } catch (Exception e) {
            Log.e("KI2", "Unable to publish notification: " + e);
        }

        return false;
    }

}
