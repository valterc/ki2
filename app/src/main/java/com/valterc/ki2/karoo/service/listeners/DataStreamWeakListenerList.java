package com.valterc.ki2.karoo.service.listeners;

import android.annotation.SuppressLint;
import android.util.Log;

import java.util.WeakHashMap;
import java.util.function.Consumer;

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

    public void removeListener(Consumer<TData> consumer) {
        if (consumer == null) {
            return;
        }

        listeners.remove(consumer);
    }

    public void pushData(TData data) {
        pushData(data, true);
    }

    public void pushData(TData data, boolean keepData) {
        if (keepData) {
            lastData = data;
        }

        listeners.keySet().forEach(c -> {
            try {
                c.accept(data);
            } catch (Exception e) {
                Log.e("KI2", "Error during callback", e);
            }
        });
    }
}
