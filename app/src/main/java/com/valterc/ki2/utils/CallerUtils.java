package com.valterc.ki2.utils;

import android.annotation.SuppressLint;
import android.util.Log;

import java.util.function.Supplier;

@SuppressLint("LogNotTimber")
public final class CallerUtils {

    private CallerUtils() {
    }

    public static <T> T safeWrap(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            Log.w("KI2", "Exception from supplied: " + e);
            return null;
        }
    }

}
