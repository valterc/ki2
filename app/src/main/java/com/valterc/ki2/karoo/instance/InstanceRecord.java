package com.valterc.ki2.karoo.instance;

public class InstanceRecord {

    private final Object instance;
    private long lastAccessTimestamp;

    public InstanceRecord(Object instance) {
        this.instance = instance;
        this.lastAccessTimestamp = System.currentTimeMillis();
    }

    public long getLastAccessTimestamp() {
        return lastAccessTimestamp;
    }

    public Object getInstance(){
        lastAccessTimestamp = System.currentTimeMillis();
        return instance;
    }

    @SuppressWarnings("unchecked")
    public <T> T getInstance(Class<T> clazz) {
        Object object = getInstance();
        if (clazz.isInstance(object)) {
            return (T) object;
        }

        return null;
    }

}
