package com.valterc.ki2.utils;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import timber.log.Timber;

@SuppressLint("LogNotTimber")
public class SafeHandler extends Handler {

    public SafeHandler(@NonNull Looper looper) {
        super(looper);
    }

    @Override
    public void dispatchMessage(@NonNull Message msg) {
        try {
            super.dispatchMessage(msg);
        } catch (Exception e) {
            String processName = ProcessUtils.getProcessName();

            if (processName != null && processName.contains("com.valter.ki2")) {
                Timber.e(e, "Error in handler invocation");
            } else {
                Log.w("KI2", "Error in handler invocation", e);
            }
        }
    }
}
