package com.valterc.ki2.karoo.instance;

import android.os.Handler;
import android.os.Looper;

import androidx.core.util.Supplier;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
        InstanceRecord instanceRecord = instanceMap.get(key);
        if (instanceRecord == null) {
            return null;
        }

        return instanceRecord.getInstance();
    }

    public Object getOrComputeInstance(String key, Supplier<?> instanceSupplier) {
        InstanceRecord instanceRecord = instanceMap.get(key);
        if (instanceRecord != null)
        {
            return instanceRecord.getInstance();
        }

        instanceRecord = new InstanceRecord(instanceSupplier.get());
        instanceMap.put(key, instanceRecord);
        return instanceRecord.getInstance();
    }

    public <T> T getInstance(String key, Class<T> clazz){
        InstanceRecord instanceRecord = instanceMap.get(key);
        if (instanceRecord == null) {
            return null;
        }

        return instanceRecord.getInstance(clazz);
    }

    public <T> T getOrComputeInstance(String key, Class<T> clazz, Supplier<T> instanceSupplier){
        InstanceRecord instanceRecord = instanceMap.get(key);
        if (instanceRecord != null)
        {
            return instanceRecord.getInstance(clazz);
        }

        instanceRecord = new InstanceRecord(instanceSupplier.get());
        instanceMap.put(key, instanceRecord);
        return instanceRecord.getInstance(clazz);
    }

    public void putInstance(String key, Object instance) {
        instanceMap.put(key, new InstanceRecord(instance));
    }

    private void cleanInstances() {
        try {
            long currentTime = System.currentTimeMillis();
            Iterator<Map.Entry<String, InstanceRecord>> iterator = instanceMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, InstanceRecord> entry = iterator.next();
                if (currentTime - entry.getValue().getLastAccessTimestamp() > INSTANCE_CLEAN_MS) {
                    iterator.remove();
                }
            }
        } finally {
            this.handler.postDelayed(this::cleanInstances, HANDLER_DELAY_MS);
        }
    }
}
