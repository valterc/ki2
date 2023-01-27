package com.valterc.ki2.utils.function;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.Nullable;

@SuppressLint("LogNotTimber")
public final class FunctionUtils {

    private FunctionUtils() {
    }

    /**
     * Invokes a supplier while ignoring exceptions. If the supplier throws and exception then this method returns <code>null</code>.
     *
     * @param supplier  Supplier to invoke.
     * @param <TReturn> Type of the value from the supplier.
     * @return Value returned from the supplier or <code>null</code> if the supplier threw an exception.
     */
    @Nullable
    public static <TReturn> TReturn safeInvoke(ThrowingSupplier<TReturn> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            Log.w("KI2", "Exception from supplier: " + e);
            return null;
        }
    }

}
