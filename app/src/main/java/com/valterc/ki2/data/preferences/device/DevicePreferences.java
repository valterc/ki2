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
     * @param context  Ki2 application context. Cannot be a context generated from another process.
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

    /**
     * Set enabled status of the device.
     *
     * @param enabled True to enable the device, False to disable.
     */
    public void setEnabled(boolean enabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(context.getString(R.string.preference_device_enabled), enabled);
        editor.apply();
    }

    /**
     * Indicates if the device to configured to only receive switch events.
     *
     * @return True if the device is configured to only receive switch events, False otherwise.
     */
    public boolean isSwitchEventsOnly() {
        return sharedPreferences.getBoolean(context.getString(R.string.preference_device_switch_events_only), context.getResources().getBoolean(R.bool.default_preference_device_switch_events_only));
    }

    /**
     * Set the device to receive switch events only.
     *
     * @param switchEventsOnly True to only receive switch events, False to receive all data.
     */
    public void setSwitchEventsOnly(boolean switchEventsOnly) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(context.getString(R.string.preference_device_switch_events_only), switchEventsOnly);
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

    /**
     * Get the priority of the device. Top priority is given to devices with lower priority value, a priority of 0 is the first.
     *
     * @return Priority of the device.
     */
    public int getPriority() {
        return sharedPreferences.getInt(context.getString(R.string.preference_device_priority), Integer.MAX_VALUE);
    }

    /**
     * Set the priority of the device. Top priority is given to devices with lower priority value, a priority of 0 is the first.
     *
     * @param priority Priority of the device. Cannot be a negative number.
     */
    public void setPriority(int priority) {
        if (priority < 0) {
            throw new RuntimeException("Priority must not be negative");
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(context.getString(R.string.preference_device_priority), priority);
        editor.apply();
    }
}
