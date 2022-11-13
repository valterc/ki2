package com.valterc.ki2.karoo;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.valterc.ki2.R;

import java.net.URI;
import java.util.Objects;

import io.hammerhead.sdk.v0.SdkContext;
import timber.log.Timber;

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
                    Log.i(TAG, "Pre-load enabled, starting ride application");

                    Intent intentRideActivity = new Intent(Intent.ACTION_MAIN);
                    intentRideActivity.setClassName("io.hammerhead.rideapp", "io.hammerhead.rideapp.views.ride.RideActivity");
                    intentRideActivity.putExtra("ki2.preload", true);
                    context.startActivity(intentRideActivity);
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
