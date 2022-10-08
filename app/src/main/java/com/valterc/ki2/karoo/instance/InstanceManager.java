package com.valterc.ki2.karoo.instance;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

public class InstanceManager {

    private static final long HANDLER_DELAY_MS = 60 * 1000;
    private static final long INSTANCE_CLEAN_MS = 60 * 1000;

    private final Handler handler;
    private final ConcurrentMap<String, InstanceRecord> instanceMap;

    public InstanceManager() {
        this.handler = new Handler(Looper.getMainLooper());
        this.instanceMap = new ConcurrentHashMap<>();

        this.handler.postDelayed(this::cleanInstances, HANDLER_DELAY_MS);
    }

    public Object getInstance(String key){
        InstanceRecord instanceRecord = instanceMap.getOrDefault(key, null);
        if (instanceRecord == null) {
            return null;
        }

        return instanceRecord.getInstance();
    }

    public Object getOrComputeInstance(String key, Supplier<?> instanceSupplier) {
        return instanceMap.computeIfAbsent(key, k -> new InstanceRecord(instanceSupplier.get())).getInstance();
    }

    public <T> T getInstance(String key, Class<T> clazz){
        InstanceRecord instanceRecord = instanceMap.getOrDefault(key, null);
        if (instanceRecord == null) {
            return null;
        }

        return instanceRecord.getInstance(clazz);
    }

    public <T> T getOrComputeInstance(String key, Class<T> clazz, Supplier<T> instanceSupplier){
        return instanceMap.computeIfAbsent(key,k -> new InstanceRecord(instanceSupplier.get())).getInstance(clazz);
    }

    public void putInstance(String key, Object instance) {
        instanceMap.put(key, new InstanceRecord(instance));
    }

    private void cleanInstances() {
        try {
            long currentTime = System.currentTimeMillis();
            instanceMap.entrySet().removeIf(entry -> currentTime - entry.getValue().getLastAccessTimestamp() > INSTANCE_CLEAN_MS);
        } finally {
            this.handler.postDelayed(this::cleanInstances, HANDLER_DELAY_MS);
        }
    }
}
