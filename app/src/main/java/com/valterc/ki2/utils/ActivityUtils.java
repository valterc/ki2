package com.valterc.ki2.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.Map;

@SuppressLint("LogNotTimber")
public final class ActivityUtils {

    private ActivityUtils() {
    }

    @SuppressLint({"PrivateApi", "DiscouragedPrivateApi"})
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Activity getRunningActivity() {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);

            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);
            Map activities = (Map) activitiesField.get(activityThread);

            if (activities == null) {
                return null;
            }

            for (Object activityRecord : activities.values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);

                if (!pausedField.getBoolean(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    return (Activity) activityField.get(activityRecord);
                }
            }
        } catch (Exception e) {
            Log.e("KI2", "Unable to get activity: " + e);
            return null;
        }

        return null;
    }

}
