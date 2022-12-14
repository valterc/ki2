package com.valterc.ki2.karoo.hooks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.lang.reflect.Method;

import io.hammerhead.sdk.v0.SdkContext;

@SuppressLint("LogNotTimber")
public class KarooAudioAlertHook {

    private KarooAudioAlertHook() {
    }

    public static boolean triggerLowBatteryAudioAlert(SdkContext context) {
        return triggerLowBatteryAudioAlert_1(context) || triggerLowBatteryAudioAlert_2(context);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static boolean triggerLowBatteryAudioAlert_1(SdkContext context) {
        try {
            Class<? extends Enum> audioAlertClass = (Class<? extends Enum>) Class.forName("io.hammerhead.datamodels.profiles.AudioAlert");
            Enum audioAlertSensorBatteryLow = Enum.valueOf(audioAlertClass, "SENSOR_BATTERY_LOW");

            Method[] methodsAudioAlert = audioAlertClass.getMethods();

            for (Method methodBroadcast: methodsAudioAlert) {
                if (methodBroadcast.getParameterTypes().length == 2) {
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

    private static boolean triggerLowBatteryAudioAlert_2(SdkContext context) {
        Intent intent = new Intent();
        intent.setAction("io.hammerhead.action.AUDIO_ALERT");
        intent.putExtra("type", 3);
        context.sendBroadcast(intent);
        return true;
    }

}
