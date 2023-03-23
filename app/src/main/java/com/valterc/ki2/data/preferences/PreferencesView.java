package com.valterc.ki2.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.preference.PreferenceManager;

import com.valterc.ki2.R;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
     * Get the audio alert for when shifting into the lowest gear.
     *
     * @param context Ki2 application context. Cannot be a context generated from another package.
     * @return Audio alert name for when shifting into the lowest gear.
     */
    public String getAudioAlertLowestGearEnabled(Context context) {
        return getString(context.getString(R.string.preference_audio_alert_lowest_gear),
                context.getString(R.string.default_preference_audio_alert_shifting_limit));
    }

    /**
     * Get the audio alert for when shifting into the highest gear.
     *
     * @param context Ki2 application context. Cannot be a context generated from another package.
     * @return Audio alert name for when shifting into the highest gear.
     */
    public String getAudioAlertHighestGearEnabled(Context context) {
        return getString(context.getString(R.string.preference_audio_alert_highest_gear),
                context.getString(R.string.default_preference_audio_alert_shifting_limit));
    }

    /**
     * Get the audio alert for when attempting to shift over the gear limit.
     *
     * @param context Ki2 application context. Cannot be a context generated from another package.
     * @return Audio alert name for shifting limit.
     */
    public String getAudioAlertShiftingLimit(Context context) {
        return getString(context.getString(R.string.preference_audio_alert_shifting_limit),
                context.getString(R.string.default_preference_audio_alert_shifting_limit));
    }

    /**
     * Get the audio alert for when the next shift can trigger a synchronized shift.
     *
     * @param context Ki2 application context. Cannot be a context generated from another package.
     * @return Audio alert name for synchronized shift.
     */
    public String getAudioAlertUpcomingSynchroShift(Context context) {
        return getString(context.getString(R.string.preference_audio_alert_upcoming_synchro_shift),
                context.getString(R.string.default_preference_audio_alert_upcoming_synchro_shift));
    }

    /**
     * Delay between audio alerts in milliseconds.
     *
     * @param context Ki2 application context. Cannot be a context generated from another package.
     * @return Delay between audio alerts in milliseconds.
     */
    public int getDelayBetweenAudioAlerts(Context context) {
        return Integer.parseInt(getString(context.getString(R.string.preference_audio_alert_delay),
                context.getString(R.string.default_preference_audio_alert_delay)));
    }

    /**
     * Indicates if the overlay is enabled.
     *
     * @param context Ki2 application context. Cannot be a context generated from another package.
     * @return True if the overlay is enabled, False otherwise.
     */
    public boolean isOverlayEnabled(Context context) {
        return getBoolean(context.getString(R.string.preference_overlay_enabled), () -> context.getResources().getBoolean(R.bool.default_preference_overlay_enabled));
    }

    /**
     * Get overlay triggers.
     *
     * @param context Ki2 application context. Cannot be a context generated from another package.
     * @return Set with overlay triggers, can be empty.
     */
    public Set<String> getOverlayTriggers(Context context) {
        return getStringSet(context.getString(R.string.preference_overlay_triggers), () -> new HashSet<>(Arrays.asList(context.getResources().getStringArray(R.array.preference_values_overlay_triggers))));
    }

    /**
     * Get the display duration of the overlay.
     *
     * @param context Ki2 application context. Cannot be a context generated from another package.
     * @return Overlay display duration in milliseconds.
     */
    public int getOverlayDuration(Context context) {
        return Integer.parseInt(getString(context.getString(R.string.preference_overlay_duration), () -> context.getString(R.string.default_preference_overlay_duration)));
    }

    /**
     * Get the overlay theme.
     *
     * @param context Ki2 application context. Cannot be a context generated from another package.
     * @return Overlay theme.
     */
    public String getOverlayTheme(Context context) {
        return getString(context.getString(R.string.preference_overlay_theme), () -> context.getString(R.string.default_preference_overlay_theme));
    }

    /**
     * Get the overlay opacity.
     *
     * @param context Ki2 application context. Cannot be a context generated from another package.
     * @return Overlay opacity.
     */
    public float getOverlayOpacity(Context context) {
        float opacity = getFloat(context.getString(R.string.preference_overlay_opacity), () -> (float)context.getResources().getInteger(R.integer.default_preference_overlay_opacity));
        return Math.min(1, Math.max(opacity, 0));
    }

}
