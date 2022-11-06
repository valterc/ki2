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

    public static void triggerLowBatteryAudioAlert(SdkContext context) {
        boolean result = triggerLowBatteryAudioAlert_1(context);

        if (!result) {
            triggerLowBatteryAudioAlert_2(context);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static boolean triggerLowBatteryAudioAlert_1(SdkContext context) {
        try {
            Class<? extends Enum> audioAlertClass = (Class<? extends Enum>) Class.forName("io.hammerhead.datamodels.profiles.AudioAlert");
            Enum audioAlertSensorBatteryLow = Enum.valueOf(audioAlertClass, "SENSOR_BATTERY_LOW");

            Method handleAlertMethod = audioAlertClass.getMethod("broadcast", Context.class, String.class);
            handleAlertMethod.invoke(audioAlertSensorBatteryLow, context.getBaseContext(), null);
        } catch (Exception e) {
            Log.e("KI2", "Unable to trigger audio alert: " + e);
            return false;
        }

        return true;
    }

    private static void triggerLowBatteryAudioAlert_2(SdkContext context) {
        Intent intent = new Intent();
        intent.setAction("io.hammerhead.action.AUDIO_ALERT");
        intent.putExtra("type", 3);
        context.sendBroadcast(intent);
    }

}
