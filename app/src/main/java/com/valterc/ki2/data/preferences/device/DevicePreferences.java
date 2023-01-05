package com.valterc.ki2.data.preferences.device;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.valterc.ki2.R;
import com.valterc.ki2.data.device.DeviceId;

public class DevicePreferences {

    private final Context context;
    private final DeviceId deviceId;
    private final SharedPreferences sharedPreferences;

    /**
     * Generate a new device preference view.
     *
     * @param context Ki2 application context. Cannot be a context generated from another process.
     * @param deviceId Device identifier.
     */
    public DevicePreferences(Context context, DeviceId deviceId) {
        this.context = context;
        this.deviceId = deviceId;
        this.sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_param_device, deviceId.getUid()), Context.MODE_PRIVATE);
    }

    /**
     * Indicates if the device is enabled.
     *
     * @return True if the device is enabled, false otherwise.
     */
    public boolean isEnabled() {
        return sharedPreferences.getBoolean(context.getString(R.string.preference_device_enabled), context.getResources().getBoolean(R.bool.default_preference_device_enabled));
    }

    public void setEnabled(boolean enabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(context.getString(R.string.preference_device_enabled), enabled);
        editor.apply();
    }

    /**
     * Get the name for the device.
     *
     * @return String with name for the device.
     */
    @Nullable
    public String getName() {
        return sharedPreferences.getString(context.getString(R.string.preference_device_name), context.getString(R.string.text_param_di2_name, deviceId.getName()));
    }

    public void setName(String name) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(context.getString(R.string.preference_device_name), name != null && name.trim().length() == 0 ? null : name);
        editor.apply();
    }

}
