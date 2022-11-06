package com.valterc.ki2.karoo.hooks;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;

import java.lang.reflect.Constructor;

@SuppressLint("LogNotTimber")
public class KarooNotificationHook {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Object buildSensorLowBatteryNotification(String deviceName) {
        try {
            Class<? extends Enum> notificationTypeClass = (Class<? extends Enum>) Class.forName("io.hammerhead.datamodels.ride.models.NotificationType");
            Enum notificationTypeSensorBatteryLow = Enum.valueOf(notificationTypeClass, "SENSOR_BATTERY_LOW");

            Class<?> notificationClass = Class.forName("io.hammerhead.datamodels.ride.models.Notification");
            Constructor<?> notificationConstructor = notificationClass.getConstructor(String.class, notificationTypeClass, boolean.class, String.class, Intent.class);

            return notificationConstructor.newInstance("ki2-battery-" + deviceName, notificationTypeSensorBatteryLow, false, deviceName, null);
        } catch (Exception e) {
            Log.e("KI2", "Unable to create notification: " + e);
        }

        return null;
    }

}
