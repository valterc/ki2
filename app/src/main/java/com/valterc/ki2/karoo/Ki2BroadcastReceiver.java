package com.valterc.ki2.karoo;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

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
                Log.i(TAG, "Boot completed, attempt pre-load");

                for (int i = 0; i < 2; i++) {
                    try {
                        Thread.sleep(2500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction("io.hammerhead.intent.action.RIDE_APP_PRELOAD");
                    broadcastIntent.setData(Uri.parse("package:" + context.getPackageName()));
                    context.sendBroadcast(broadcastIntent);

                    try {
                        Thread.sleep(15_000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

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
