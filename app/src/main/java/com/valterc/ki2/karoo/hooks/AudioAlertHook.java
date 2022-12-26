package com.valterc.ki2.karoo.hooks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.lang.reflect.Method;

import io.hammerhead.sdk.v0.SdkContext;
import kotlin.Lazy;
import kotlin.LazyKt;

@SuppressWarnings({"UnusedReturnValue", "unchecked", "rawtypes"})
@SuppressLint("LogNotTimber")
public class AudioAlertHook {

    private static final Lazy<Class<? extends Enum>> ENUM_AUDIO_ALERT = LazyKt.lazy(() -> {
        try {
            return (Class<? extends Enum>) Class.forName("io.hammerhead.datamodels.profiles.AudioAlert");
        } catch (Exception e) {
            Log.w("KI2", "Unable to get Audio Alert enum", e);
        }

        return null;
    });

    private AudioAlertHook() {
    }

    private static boolean triggerAudioAlert_1(SdkContext context, String enumName) {
        try {
            Class<? extends Enum> audioAlertClass = ENUM_AUDIO_ALERT.getValue();
            if (audioAlertClass == null) {
                return false;
            }

            Enum audioAlertSensorBatteryLow = Enum.valueOf(audioAlertClass, enumName);

            Method[] methodsAudioAlert = audioAlertClass.getMethods();

            for (Method methodBroadcast: methodsAudioAlert) {
                if (methodBroadcast.getParameterCount() == 2) {
                    Class<?>[] parameterTypes = methodBroadcast.getParameterTypes();
                    if (parameterTypes[0] == Context.class && parameterTypes[1] == String.class) {
                        methodBroadcast.invoke(audioAlertSensorBatteryLow, context.getBaseContext(), null);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            Log.e("KI2", "Unable to trigger audio alert using method 1: " + e);
        }

        return false;
    }

    private static boolean triggerAudioAlert_2(SdkContext context, String enumName) {
        try {
            Class<? extends Enum> audioAlertClass = ENUM_AUDIO_ALERT.getValue();
            if (audioAlertClass == null) {
                return false;
            }

            Enum audioAlertSensorBatteryLow = Enum.valueOf(audioAlertClass, enumName);

            Intent intent = new Intent();
            intent.setAction("io.hammerhead.action.AUDIO_ALERT");
            intent.putExtra("type", audioAlertSensorBatteryLow.ordinal());
            context.sendBroadcast(intent);
            return true;
        } catch (Exception e) {
            Log.e("KI2", "Unable to trigger audio alert using method 2: " + e);
        }

        return false;
    }

    private static boolean triggerAudioAlert_3(SdkContext context, int enumValue) {
        Intent intent = new Intent();
        intent.setAction("io.hammerhead.action.AUDIO_ALERT");
        intent.putExtra("type", enumValue);
        context.sendBroadcast(intent);
        return true;
    }

    /**
     * Trigger a low battery audio alert.
     *
     * @param context Sdk context.
     * @return True if the alert was triggered, False otherwise.
     */
    public static boolean triggerLowBatteryAudioAlert(SdkContext context) {
        return triggerAudioAlert_1(context, "SENSOR_BATTERY_LOW") ||
                triggerAudioAlert_2(context, "SENSOR_BATTERY_LOW") ||
                triggerAudioAlert_3(context, 3);
    }

    /**
     * Trigger a low battery audio alert.
     *
     * @param context Sdk context.
     * @return True if the alert was triggered, False otherwise.
     */
    public static boolean triggerGearAudioAlert(SdkContext context) {
        return triggerAudioAlert_1(context, "AUTO_LAP") ||
                triggerAudioAlert_2(context, "AUTO_LAP") ||
                triggerAudioAlert_3(context, 17);
    }

}
