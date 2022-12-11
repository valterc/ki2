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

    private static boolean showNotification_1(SdkContext context, Object notification) {
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
                    method.invoke(notificationSubject, notification);
                    return true;
                }
            }

            throw new Exception("Unable to hook into notification publisher");
        } catch (Exception e) {
            Log.w("KI2", "Unable to publish notification using method 1: " + e);
        }

        return false;
    }

    private static boolean showNotification_2(SdkContext context, Object notification) {
        try {
            Class<?> classActivityServiceApplication = Class.forName("io.hammerhead.activityservice.ActivityServiceApplication");
            Field[] fieldsInActivityServiceApplication = classActivityServiceApplication.getDeclaredFields();

            for (Field fieldActivityComponent: fieldsInActivityServiceApplication) {
                fieldActivityComponent.setAccessible(true);
                Object activityComponent = fieldActivityComponent.get(context.getBaseContext());

                if (activityComponent == null) {
                    continue;
                }

                Method[] methodsInActivityComponent = activityComponent.getClass().getDeclaredMethods();
                for (Method method: methodsInActivityComponent) {
                    Field[] fieldsInNotificationController = method.getReturnType().getDeclaredFields();

                    if (fieldsInNotificationController.length == 0) {
                        continue;
                    }

                    Object notificationController = method.invoke(activityComponent);
                    for (Field fieldNotificationSubject: fieldsInNotificationController) {

                        if (fieldNotificationSubject.getType().getTypeParameters().length == 1 &&
                                fieldNotificationSubject.getGenericType().toString().contains("Notification")) {

                            fieldNotificationSubject.setAccessible(true);
                            Object notificationSubject = fieldNotificationSubject.get(notificationController);

                            if (notificationSubject == null) {
                                continue;
                            }

                            Method[] methodsInPublishSubject = notificationSubject.getClass().getDeclaredMethods();

                            for (Method methodOnNext: methodsInPublishSubject) {
                                Type[] types = methodOnNext.getGenericParameterTypes();
                                if (types.length == 1 && types[0].toString().equals("T")) {
                                    methodOnNext.invoke(notificationSubject, notification);
                                    return true;
                                }
                            }
                        }
                    }
                }
            }

            throw new Exception("Unable to hook into notification publisher");
        } catch (Exception e) {
            Log.w("KI2", "Unable to publish notification using method 2: " + e);
        }

        return false;
    }

    public static boolean showSensorLowBatteryNotification(SdkContext context, String deviceName) {
        Object notification = KarooNotificationHook.buildSensorLowBatteryNotification(context.getString(R.string.text_param_di2_name, deviceName));
        return showNotification_1(context, notification)
                || showNotification_2(context, notification);
    }

}
