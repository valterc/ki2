package com.valterc.ki2.karoo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import io.hammerhead.sdk.v0.SdkContext;
import timber.log.Timber;

public class Ki2BroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.d("Module loaded");
    }
}
