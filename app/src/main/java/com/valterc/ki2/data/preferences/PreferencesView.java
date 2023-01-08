package com.valterc.ki2.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.preference.PreferenceManager;

import com.valterc.ki2.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@SuppressWarnings({"unchecked", "unused"})
public class PreferencesView implements Parcelable {

    private final Map<String, ?> preferenceMap;

    public static final Parcelable.Creator<PreferencesView> CREATOR = new Parcelable.Creator<PreferencesView>() {
        public PreferencesView createFromParcel(Parcel in) {
            return new PreferencesView(in);
        }

        public PreferencesView[] newArray(int size) {
            return new PreferencesView[size];
        }
    };

    private PreferencesView(Parcel in) {
        preferenceMap = new HashMap<>();
        in.readMap(preferenceMap, PreferencesView.class.getClassLoader());
    }

    /**
     * Generate a new preference view.
     *
     * @param context Ki2 application context. Cannot be a context generated from another process.
     */
    public PreferencesView(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferenceMap = preferences.getAll();
    }

    /**
     * Generate a new preference view.
     *
     * @param preferences Default shared preferences from Ki2 application context.
     */
    public PreferencesView(SharedPreferences preferences) {
        preferenceMap = preferences.getAll();
    }

    /**
     * Generate a new empty preference view with no preferences set.
     */
    public PreferencesView() {
        preferenceMap = new HashMap<>();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeMap(preferenceMap);
    }

    @Override
    public int describeContents() {
        return 0;
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
     * Indicates if the preload setting is active.
     *
     * @param context Ki2 application context. Cannot be a context generated from another package.
     * @return True if the preload setting is active, false otherwise.
     */
    public boolean getPreLoad(Context context) {
        return getBoolean(context.getString(R.string.preference_pre_load), context.getResources().getBoolean(R.bool.default_preference_pre_load));
    }

    /**
     * Get the battery level that should be considered as low battery.
     *
     * @param context Ki2 application context. Cannot be a context generated from another package.
     * @return Integer value corresponding to the battery level in percentage.
     * Null to indicate that there is no low battery level.
     */
    public Integer getBatteryLevelLow(Context context) {
        String value = getString(context.getString(R.string.preference_battery_level_low), context.getString(R.string.default_preference_battery_level_low));

        if (value == null || value.equalsIgnoreCase(context.getString(R.string.value_preference_battery_level_off))) {
            return null;
        }

        return Integer.valueOf(value);
    }

    /**
     * Get the battery level that should be considered as critical battery.
     *
     * @param context Ki2 application context. Cannot be a context generated from another package.
     * @return Integer value corresponding to the battery level in percentage.
     * Null to indicate that there is no critical battery level.
     */
    public Integer getBatteryLevelCritical(Context context) {
        String value = getString(context.getString(R.string.preference_battery_level_critical), context.getString(R.string.default_preference_battery_level_critical));

        if (value == null || value.equalsIgnoreCase(context.getString(R.string.value_preference_battery_level_off))) {
            return null;
        }

        return Integer.valueOf(value);
    }

    /**
     * Indicates if the audio alert for when shifting into the lowest gear is enabled.
     *
     * @param context Ki2 application context. Cannot be a context generated from another package.
     * @return True if the audio alert for when shifting into the lowest gear is enabled, False otherwise.
     */
    public boolean isAudioAlertLowestGearEnabled(Context context) {
        return getBoolean(context.getString(R.string.preference_audio_alert_lowest_gear),
                context.getResources().getBoolean(R.bool.default_preference_audio_alert));
    }

    /**
     * Indicates if the audio alert for when shifting into the highest gear is enabled.
     *
     * @param context Ki2 application context. Cannot be a context generated from another package.
     * @return True if the audio alert for when shifting into the highest gear is enabled, False otherwise.
     */
    public boolean isAudioAlertHighestGearEnabled(Context context) {
        return getBoolean(context.getString(R.string.preference_audio_alert_highest_gear),
                context.getResources().getBoolean(R.bool.default_preference_audio_alert));
    }

    /**
     * Indicates if the audio alert for when attempting to shift over the gear limit is enabled.
     *
     * @param context Ki2 application context. Cannot be a context generated from another package.
     * @return True if the audio alert for when attempting to shift over the gear limit is enabled, False otherwise.
     */
    public boolean isAudioAlertShiftingLimit(Context context) {
        return getBoolean(context.getString(R.string.preference_audio_alert_shifting_limit),
                context.getResources().getBoolean(R.bool.default_preference_audio_alert));
    }

    /**
     * Indicates if the audio alert for when the next shift can trigger a synchronized shift is enabled.
     *
     * @param context Ki2 application context. Cannot be a context generated from another package.
     * @return True if the audio alert for when the next shift can trigger a synchronized shift is enabled, False otherwise.
     */
    public boolean isAudioAlertUpcomingSynchroShift(Context context) {
        return getBoolean(context.getString(R.string.preference_audio_alert_upcoming_synchro_shift),
                context.getResources().getBoolean(R.bool.default_preference_audio_alert));
    }

}
