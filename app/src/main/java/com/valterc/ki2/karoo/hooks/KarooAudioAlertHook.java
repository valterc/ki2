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
        Intent intent = new Intent();
        intent.setAction("io.hammerhead.action.AUDIO_ALERT");
        intent.putExtra("type", 3);
        context.sendBroadcast(intent);
    }

}
