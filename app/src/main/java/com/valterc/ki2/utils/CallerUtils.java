package com.valterc.ki2.utils;

import android.annotation.SuppressLint;
import android.util.Log;

import java.util.function.Supplier;

@SuppressLint("LogNotTimber")
public final class CallerUtils {

    private CallerUtils() {
    }

    public static <TReturn> TReturn safeWrap(Supplier<TReturn> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            Log.w("KI2", "Exception from supplier: " + e);
            return null;
        }
    }

}
