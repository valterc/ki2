package com.valterc.ki2.karoo;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.valterc.ki2.R;
import com.valterc.ki2.karoo.hooks.RideActivityHook;

public class Ki2BroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "Ki2BroadcastReceiver";

    @SuppressLint("LogNotTimber")
    @Override
    public void onReceive(Context context, Intent intent) {

        switch (intent.getAction()) {

            case "android.intent.action.BOOT_COMPLETED":
                Log.i(TAG, "Boot completed");

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                boolean preload = preferences.getBoolean(context.getString(R.string.preference_pre_load), false);
                if (preload) {
                    Log.i(TAG, "Pre-load enabled, preloading ride application");
                    RideActivityHook.preload(context);
                }

                break;

            case "io.hammerhead.sdk.INITIALIZE":
                Log.i(TAG, "Module loaded");
                break;

            default:
                Log.w(TAG, "Unrecognized broadcast action: " + intent.getAction());
                break;

        }

    }
}
