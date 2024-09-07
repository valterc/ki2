package com.valterc.ki2.karoo.hooks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.valterc.ki2.ant.AntSettings;

@SuppressWarnings("UnusedReturnValue")
@SuppressLint("LogNotTimber")
public class ActivityServiceAntHook {

    private ActivityServiceAntHook() {
    }

    public static boolean ensureAntEnabled(Context context) {
        try {
            if (AntSettings.isAntEnabled(context)) {
                return true;
            }

            AntSettings.enableAnt(context);
            Log.i("KI2", "Enabled ANT");
            return true;
        } catch (Exception e) {
            Log.w("KI2", "Error enabling Ant", e);
        }

        return false;
    }

}
