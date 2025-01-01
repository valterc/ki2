package com.valterc.ki2.karoo.service.listeners;

import android.annotation.SuppressLint;
import android.util.Log;

import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;

@SuppressLint("LogNotTimber")
public class BiDataKeyedStreamWeakListenerList<TKey, TValue> {

    private final WeakHashMap<BiConsumer<TKey, TValue>, Boolean> listeners;
    private final HashMap<TKey, TValue> values;

    public BiDataKeyedStreamWeakListenerList() {
        this.listeners = new WeakHashMap<>();
        this.values = new HashMap<>();
    }

    public boolean hasListeners() {
        return !listeners.isEmpty();
    }

    public void addListener(BiConsumer<TKey, TValue> consumer) {
        if (consumer == null) {
            return;
        }

        listeners.put(consumer, null);

        for (var key: values.keySet()) {
            TValue value = values.get(key);
            if (value != null) {
                consumer.accept(key, value);
            }
        }
    }

    public void removeListener(BiConsumer<TKey, TValue> consumer) {
        if (consumer == null) {
            return;
        }

        listeners.remove(consumer);
    }

    public void pushData(TKey key, TValue value) {
        pushData(key, value, true);
    }

    public void pushData(TKey key, TValue value, boolean keepData) {
        if (keepData) {
            values.put(key, value);
        }

        listeners.keySet().forEach(c -> {
            try {
                c.accept(key, value);
            } catch (Exception e) {
                Log.e("KI2", "Error during callback", e);
            }
        });
    }
}
