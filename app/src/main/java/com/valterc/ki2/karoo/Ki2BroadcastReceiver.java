package com.valterc.ki2.karoo;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.valterc.ki2.activities.update.UpdateActivity;
import com.valterc.ki2.data.update.OngoingUpdateStateInfo;
import com.valterc.ki2.data.update.UpdateStateStore;

public class Ki2BroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "KI2";

    @SuppressLint("LogNotTimber")
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null) {
            return;
        }

        if (intent.getAction().equals("io.hammerhead.sdk.INITIALIZE")) {
            Log.i(TAG, "Module loaded");

            OngoingUpdateStateInfo ongoingUpdateStateInfo = UpdateStateStore.getAndClearOngoingUpdateState(context);
            if (ongoingUpdateStateInfo != null) {
                Log.i(TAG, "Starting update activity after update complete");
                Intent intentUpdateActivity = new Intent(context, UpdateActivity.class);
                intentUpdateActivity.putExtra(OngoingUpdateStateInfo.class.getSimpleName(), ongoingUpdateStateInfo);
                intentUpdateActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intentUpdateActivity);
            }
        } else {
            Log.w(TAG, "Unrecognized broadcast action: " + intent.getAction());
        }

    }
}
