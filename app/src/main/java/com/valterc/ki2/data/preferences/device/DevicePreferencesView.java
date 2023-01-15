package com.valterc.ki2.data.preferences.device;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.valterc.ki2.R;
import com.valterc.ki2.data.device.DeviceId;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@SuppressWarnings({"unchecked", "unused"})
public class DevicePreferencesView implements Parcelable {

    private final DeviceId deviceId;
    private final Map<String, ?> preferenceMap;

    public static final Creator<DevicePreferencesView> CREATOR = new Creator<DevicePreferencesView>() {
        public DevicePreferencesView createFromParcel(Parcel in) {
            return new DevicePreferencesView(in);
        }

        public DevicePreferencesView[] newArray(int size) {
            return new DevicePreferencesView[size];
        }
    };

    private DevicePreferencesView(Parcel in) {
        preferenceMap = new HashMap<>();
        in.readMap(preferenceMap, DevicePreferencesView.class.getClassLoader());
        deviceId = in.readParcelable(DeviceId.class.getClassLoader());
    }

    /**
     * Generate a new device preference view.
     *
     * @param context Ki2 application context. Cannot be a context generated from another process.
     * @param deviceId Device identifier.
     */
    public DevicePreferencesView(Context context, DeviceId deviceId) {
        this(context.getSharedPreferences(context.getString(R.string.preference_param_device, deviceId.getUid()), Context.MODE_PRIVATE), deviceId);
    }

    /**
     * Generate a new device preference view.
     *
     * @param preferences Device shared preferences from Ki2 application context.
     */
    public DevicePreferencesView(SharedPreferences preferences, DeviceId deviceId) {
        this.deviceId = deviceId;
        preferenceMap = preferences.getAll();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeMap(preferenceMap);
        out.writeParcelable(deviceId, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public DeviceId getDeviceId() {
        return deviceId;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        if (preferenceMap.containsKey(key)) {
            return (boolean) preferenceMap.get(key);
        }

        return defaultValue;
    }

    public boolean getBoolean(String key, Supplier<Boolean> defaultValueSupplier) {
        if (preferenceMap.containsKey(key)) {
            return (boolean) preferenceMap.get(key);
        }

        return defaultValueSupplier.get();
    }

    public int getInt(String key, int defaultValue) {
        if (preferenceMap.containsKey(key)) {
            return (int) preferenceMap.get(key);
        }

        return defaultValue;
    }

    public int getInt(String key, Supplier<Integer> defaultValueSupplier) {
        if (preferenceMap.containsKey(key)) {
            return (int) preferenceMap.get(key);
        }

        return defaultValueSupplier.get();
    }

    public String getString(String key, String defaultValue) {
        if (preferenceMap.containsKey(key)) {
            return (String) preferenceMap.get(key);
        }

        return defaultValue;
    }

    public String getString(String key, Supplier<String> defaultValueSupplier) {
        if (preferenceMap.containsKey(key)) {
            return (String) preferenceMap.get(key);
        }

        return defaultValueSupplier.get();
    }

    public float getFloat(String key, float defaultValue) {
        if (preferenceMap.containsKey(key)) {
            return (float) preferenceMap.get(key);
        }

        return defaultValue;
    }

    public float getFloat(String key, Supplier<Float> defaultValueSupplier) {
        if (preferenceMap.containsKey(key)) {
            return (float) preferenceMap.get(key);
        }

        return defaultValueSupplier.get();
    }

    public long getLong(String key, long defaultValue) {
        if (preferenceMap.containsKey(key)) {
            return (long) preferenceMap.get(key);
        }

        return defaultValue;
    }

    public long getLong(String key, Supplier<Long> defaultValueSupplier) {
        if (preferenceMap.containsKey(key)) {
            return (long) preferenceMap.get(key);
        }

        return defaultValueSupplier.get();
    }

    public Set<String> getStringSet(String key, Set<String> defaultValue) {
        if (preferenceMap.containsKey(key)) {
            return (Set<String>) preferenceMap.get(key);
        }

        return defaultValue;
    }

    public Set<String> getStringSet(String key, Supplier<Set<String>> defaultValueSupplier) {
        if (preferenceMap.containsKey(key)) {
            return (Set<String>) preferenceMap.get(key);
        }

        return defaultValueSupplier.get();
    }

    /**
     * Indicates if the device is enabled.
     *
     * @param context Ki2 application context. Cannot be a context generated from another package.
     * @return True if the device is enabled, false otherwise.
     */
    public boolean isEnabled(Context context) {
        return getBoolean(context.getString(R.string.preference_device_enabled), () -> context.getResources().getBoolean(R.bool.default_preference_device_enabled));
    }

    /**
     * Get the name for the device.
     *
     * @param context Ki2 application context. Cannot be a context generated from another package.
     * @return String with name for the device.
     */
    @Nullable
    public String getName(Context context) {
        return getString(context.getString(R.string.preference_device_name), context.getString(R.string.text_param_di2_name, deviceId.getName()));
    }

}
