package com.valterc.ki2.data.preferences.device;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.preferences.PreferencesView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class DevicePreferencesStore {

    private final Context context;
    private final BiConsumer<DeviceId, DevicePreferencesView> preferencesListener;
    private final Map<DeviceId, DevicePreferencesEntry> preferencesEntryMap;

    public DevicePreferencesStore(Context context, BiConsumer<DeviceId, DevicePreferencesView> preferencesListener) {
        this.context = context;
        this.preferencesListener = preferencesListener;
        this.preferencesEntryMap = new HashMap<>();
    }

    public void setDevices(List<DeviceId> devices) {
        Iterator<DeviceId> deviceIdIterator = preferencesEntryMap.keySet().iterator();
        while (deviceIdIterator.hasNext()) {
            DeviceId deviceId = deviceIdIterator.next();
            if (!devices.contains(deviceId)) {
                DevicePreferencesEntry devicePreferencesEntry = preferencesEntryMap.get(deviceId);
                if (devicePreferencesEntry != null) {
                    devicePreferencesEntry.dispose();
                }
                deviceIdIterator.remove();
            }
        }

        devices.forEach(deviceId ->
                preferencesEntryMap.computeIfAbsent(deviceId, key -> new DevicePreferencesEntry(context, key, this::onDevicePreferences)));
    }

    private void onDevicePreferences(DeviceId deviceId, DevicePreferencesView devicePreferencesView) {
        preferencesListener.accept(deviceId, devicePreferencesView);
    }

    public DevicePreferencesView getDevicePreferences(DeviceId deviceId) {
        DevicePreferencesEntry devicePreferencesEntry = preferencesEntryMap.get(deviceId);
        if (devicePreferencesEntry != null) {
            return devicePreferencesEntry.getDevicePreferences();
        }

        return null;
    }

    public Map<DeviceId, DevicePreferencesView> getDevicePreferences() {
        return preferencesEntryMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, value -> value.getValue().getDevicePreferences()));
    }

}
