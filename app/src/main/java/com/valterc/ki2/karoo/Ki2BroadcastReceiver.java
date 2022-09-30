package com.valterc.ki2.karoo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import io.hammerhead.sdk.v0.SdkContext;
import timber.log.Timber;

public class Ki2BroadcastReceiver extends BroadcastReceiver {

    private static final Timber.Tree Logger = Timber.tag(Ki2BroadcastReceiver.class.getName());

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.d("Ki2BroadcastReceiver: Module loaded");
        SdkContext sdkContext = SdkContext.buildSdkContext(context);
    }
}
