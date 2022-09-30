package com.valterc.ki2.data.device;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import timber.log.Timber;

public class DeviceStore {

    public static final String SHARED_PREFERENCES_DEVICE_STORE = "DEVICE_STORE";
    public static final String DEVICES = "Devices";

    private final Context context;
    private final Set<DeviceId> deviceSet;

    public DeviceStore(Context context) {
        this.context = context;
        this.deviceSet = load();
    }

    private Set<DeviceId> load() {
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_DEVICE_STORE, context.MODE_PRIVATE);
            String devices = sharedPreferences.getString(DEVICES, null);

            if (devices != null) {
                Set<DeviceId> deviceSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
                deviceSet.retainAll(new Gson().fromJson(devices, new TypeToken<HashSet<DeviceId>>(){}.getType()));
                return deviceSet;
            }
        } catch (Exception e) {
            Timber.e(e, "Unable to load device store data");
        }

        return Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    private void persist() {
        try {

            SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_DEVICE_STORE, context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(DEVICES, new Gson().toJson(deviceSet));
            editor.apply();
        } catch (Exception e) {
            Timber.e(e, "Unable to persist device store data");
        }
    }

    public void saveDevice(DeviceId deviceId) {
        deviceSet.add(deviceId);
        persist();
    }

    public void deleteDevice(DeviceId deviceId) {
        deviceSet.remove(deviceId);
        persist();
    }

    public Collection<DeviceId> getDevices() {
        return deviceSet;
    }

}
