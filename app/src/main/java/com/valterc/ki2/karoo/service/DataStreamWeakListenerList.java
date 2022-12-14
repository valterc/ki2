package com.valterc.ki2.karoo.service;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.core.util.Consumer;

import java.util.WeakHashMap;

@SuppressLint("LogNotTimber")
public class DataStreamWeakListenerList<TData> {

    private final WeakHashMap<Consumer<TData>, Boolean> listeners;
    private TData lastData;

    public DataStreamWeakListenerList() {
        this.listeners = new WeakHashMap<>();
    }

    public boolean hasListeners() {
        return listeners.size() != 0;
    }

    public void addListener(Consumer<TData> consumer) {
        if (consumer == null) {
            return;
        }

        listeners.put(consumer, null);

        if (lastData != null) {
            consumer.accept(lastData);
        }
    }

    public void pushData(TData data) {
        pushData(data, true);
    }

    public void pushData(TData data, boolean keepData) {
        if (keepData) {
            lastData = data;
        }

        for (Consumer<TData> c : listeners.keySet()) {
            try {
                c.accept(data);
            } catch (Exception e) {
                Log.e("KI2", "Error during callback", e);
            }
        }
    }
}
