package com.valterc.ki2.data.preferences.device;

import android.content.Context;
import android.content.SharedPreferences;

import com.valterc.ki2.R;
import com.valterc.ki2.data.device.DeviceId;

import java.util.function.BiConsumer;

public class DevicePreferencesEntry {

    @SuppressWarnings("FieldCanBeLocal")
    private final SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChanged
            = this::onSharedPreferenceChanged;

    private final BiConsumer<DeviceId, DevicePreferencesView> preferencesListener;
    private final DeviceId deviceId;
    private final SharedPreferences sharedPreferences;
    private DevicePreferencesView devicePreferencesView;

    public DevicePreferencesEntry(Context context, DeviceId deviceId, BiConsumer<DeviceId, DevicePreferencesView> preferencesListener) {
        this.deviceId = deviceId;
        this.preferencesListener = preferencesListener;

        sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_param_device, deviceId.getUid()), Context.MODE_PRIVATE);
        devicePreferencesView = new DevicePreferencesView(sharedPreferences);
        sharedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChanged);
    }

    private void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        devicePreferencesView = new DevicePreferencesView(sharedPreferences);
        preferencesListener.accept(deviceId, devicePreferencesView);
    }

    public void dispose() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChanged);
    }

    public DevicePreferencesView getDevicePreferences() {
        return devicePreferencesView;
    }
}
