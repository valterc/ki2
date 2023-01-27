package com.valterc.ki2.karoo.service.listeners;

import android.annotation.SuppressLint;
import android.util.Log;

import com.valterc.ki2.utils.function.ThrowingConsumer;

@SuppressLint("LogNotTimber")
public class ServiceCallbackRegistration<TCallback> {

    private final TCallback callback;
    private final ThrowingConsumer<TCallback> consumerRegister;
    private final ThrowingConsumer<TCallback> consumerUnregister;
    private boolean registered;

    public ServiceCallbackRegistration(TCallback callback, ThrowingConsumer<TCallback> consumerRegister, ThrowingConsumer<TCallback> consumerUnregister) {
        this.callback = callback;
        this.consumerRegister = consumerRegister;
        this.consumerUnregister = consumerUnregister;
    }

    public void register() {
        if (registered) {
            return;
        }

        try {
            consumerRegister.accept(callback);
            registered = true;
        } catch (Exception e) {
            Log.e("KI2", "Unable to register listener", e);
        }
    }

    public void unregister() {
        if (!registered) {
            return;
        }

        try {
            consumerUnregister.accept(callback);
        } catch (Exception e) {
            Log.e("KI2", "Unable to unregister listener", e);
        }

        registered = false;
    }

    public void setUnregistered() {
        registered = false;
    }

}
