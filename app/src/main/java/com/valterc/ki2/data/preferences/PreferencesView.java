package com.valterc.ki2.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.ColorInt;
import androidx.preference.PreferenceManager;

import com.valterc.ki2.R;
import com.valterc.ki2.karoo.overlay.view.builder.OverlayViewBuilderEntry;
import com.valterc.ki2.karoo.overlay.view.builder.OverlayViewBuilderRegistry;
import com.valterc.ki2.karoo.views.KarooTheme;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings({"unchecked", "unused"})
public class PreferencesView implements Parcelable {

    private final Map<String, ?> preferenceMap;

    private Integer cachedAccentColor;

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
     * Generate a new preference from a map.
     *
     * @param preferences Map with preferences.
     */
    public PreferencesView(Map<String, ?> preferences) {
        preferenceMap = new HashMap<>(preferences);
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

    private <T> T getOrCache(T value, Supplier<T> supplier, Consumer<T> consumer){
        if (value != null){
            return value;
        }

        value = supplier.get();
        consumer.accept(value);

        return value;
    }

    private boolean getBoolean(String key, boolean defaultValue) {
        if (preferenceMap.containsKey(key)) {
            return (boolean) preferenceMap.get(key);
        }

        return defaultValue;
    }

    private boolean getBoolean(String key, Supplier<Boolean> defaultValueSupplier) {
        if (preferenceMap.containsKey(key)) {
            return (boolean) preferenceMap.get(key);
        }

        return defaultValueSupplier.get();
    }

    private int getInt(String key, int defaultValue) {
        if (preferenceMap.containsKey(key)) {
            return (int) preferenceMap.get(key);
        }

        return defaultValue;
    }

    private int getInt(String key, Supplier<Integer> defaultValueSupplier) {
        if (preferenceMap.containsKey(key)) {
            return (int) preferenceMap.get(key);
        }

        return defaultValueSupplier.get();
    }

    private String getString(String key, String defaultValue) {
        if (preferenceMap.containsKey(key)) {
            return (String) preferenceMap.get(key);
        }

        return defaultValue;
    }

    private String getString(String key, Supplier<String> defaultValueSupplier) {
        if (preferenceMap.containsKey(key)) {
            return (String) preferenceMap.get(key);
        }

        return defaultValueSupplier.get();
    }

    private float getFloat(String key, float defaultValue) {
        if (preferenceMap.containsKey(key)) {
            return (float) preferenceMap.get(key);
        }

        return defaultValue;
    }

    private float getFloat(String key, Supplier<Float> defaultValueSupplier) {
        if (preferenceMap.containsKey(key)) {
            return (float) preferenceMap.get(key);
        }

        return defaultValueSupplier.get();
    }

    private long getLong(String key, long defaultValue) {
        if (preferenceMap.containsKey(key)) {
            return (long) preferenceMap.get(key);
        }

        return defaultValue;
    }

    private long getLong(String key, Supplier<Long> defaultValueSupplier) {
        if (preferenceMap.containsKey(key)) {
            return (long) preferenceMap.get(key);
        }

        return defaultValueSupplier.get();
    }

    private Set<String> getStringSet(String key, Set<String> defaultValue) {
        if (preferenceMap.containsKey(key)) {
            return (Set<String>) preferenceMap.get(key);
        }

        return defaultValue;
    }

    private Set<String> getStringSet(String key, Supplier<Set<String>> defaultValueSupplier) {
        if (preferenceMap.containsKey(key)) {
            return (Set<String>) preferenceMap.get(key);
        }

        return defaultValueSupplier.get();
    }

    /**
     * Indicates if FIT Recording setting is enabled.
     *
     * @param context Ki2 application context. Cannot be a context generated from another package.
     * @return True if FIT Recording setting is enabled, false otherwise.
     */
    public boolean isFITRecordingEnabled(Context context) {
        return getBoolean(context.getString(R.string.preference_fit_recording),
                () -> context.getResources().getBoolean(R.bool.default_preference_fit_recording));
    }

    /**
     * Indicates if ANT Recording setting is enabled.
     *
     * @param context Ki2 application context. Cannot be a context generated from another package.
     * @return True if ANT Recording setting is enabled, false otherwise.
     */
    public boolean isANTRecordingEnabled(Context context) {
        return getBoolean(context.getString(R.string.preference_ant_recording),
                () -> context.getResources().getBoolean(R.bool.default_preference_ant_recording));
    }

    /**
     * Indicates if a switch press should turn screen on.
     *
     * @param context Ki2 application context. Cannot be a context generated from another package.
     * @return True a switch press should turn screen on, false otherwise.
     */
    public boolean isSwitchTurnScreenOn(Context context) {
        return getBoolean(context.getString(R.string.preference_switch_turn_screen_on),
                () -> context.getResources().getBoolean(R.bool.default_preference_switch_turn_screen_on));
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
     * Indicates if audio alerts are enabled.
     *
     * @param context Ki2 application context. Cannot be a context generated from another package.
     * @return True if audio alerts are enabled, False otherwise.
     */
    public boolean isAudioAlertsEnabled(Context context) {
        return getBoolean(context.getString(R.string.preference_audio_alerts_enabled),
                () -> context.getResources().getBoolean(R.bool.default_preference_audio_alerts_enabled));
    }

    /**
     * Get the audio alert for when shifting into the lowest gear.
     *
     * @param context Ki2 application context. Cannot be a context generated from another package.
     * @return Audio alert name for when shifting into the lowest gear.
     */
    public String getAudioAlertLowestGear(Context context) {
        return getString(context.getString(R.string.preference_audio_alert_lowest_gear),
                context.getString(R.string.default_preference_audio_alert_shifting_limit));
    }

    /**
     * Get the audio alert for when shifting into the highest gear.
     *
     * @param context Ki2 application context. Cannot be a context generated from another package.
     * @return Audio alert name for when shifting into the highest gear.
     */
    public String getAudioAlertHighestGear(Context context) {
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
        float opacity = getFloat(context.getString(R.string.preference_overlay_opacity), () -> (float) context.getResources().getInteger(R.integer.default_preference_overlay_opacity));
        return Math.min(1, Math.max(opacity, 0));
    }

    /**
     * Get the overlay X position.
     *
     * @param context Ki2 application context. Cannot be a context generated from another package.
     * @return Overlay X position.
     */
    public int getOverlayPositionX(Context context) {
        return getInt(context.getString(R.string.preference_overlay_position_x), () -> {
            OverlayViewBuilderEntry overlayBuilder = OverlayViewBuilderRegistry.getBuilder(getOverlayTheme(context));

            if (overlayBuilder != null) {
                return overlayBuilder.getDefaultPositionX();
            }

            return context.getResources().getInteger(R.integer.default_preference_overlay_position);
        });
    }

    /**
     * Get the overlay Y position.
     *
     * @param context Ki2 application context. Cannot be a context generated from another package.
     * @return Overlay Y position.
     */
    public int getOverlayPositionY(Context context) {
        return getInt(context.getString(R.string.preference_overlay_position_y), () -> {
            OverlayViewBuilderEntry overlayBuilder = OverlayViewBuilderRegistry.getBuilder(getOverlayTheme(context));

            if (overlayBuilder != null) {
                return overlayBuilder.getDefaultPositionY();
            }

            return context.getResources().getInteger(R.integer.default_preference_overlay_position);
        });
    }

    /**
     * Indicates if the secondary overlay is enabled.
     *
     * @param context Ki2 application context. Cannot be a context generated from another package.
     * @return True if the secondary overlay is enabled, False otherwise.
     */
    public boolean isSecondaryOverlayEnabled(Context context) {
        return getBoolean(context.getString(R.string.preference_secondary_overlay_enabled), () -> context.getResources().getBoolean(R.bool.default_preference_secondary_overlay_enabled));
    }

    /**
     * Get secondary overlay triggers.
     *
     * @param context Ki2 application context. Cannot be a context generated from another package.
     * @return Set with secondary overlay triggers, can be empty.
     */
    public Set<String> getSecondaryOverlayTriggers(Context context) {
        return getStringSet(context.getString(R.string.preference_secondary_overlay_triggers), () -> new HashSet<>(Arrays.asList(context.getResources().getStringArray(R.array.default_preference_secondary_overlay_triggers))));
    }

    /**
     * Get the display duration of the secondary overlay.
     *
     * @param context Ki2 application context. Cannot be a context generated from another package.
     * @return Secondary overlay display duration in milliseconds.
     */
    public int getSecondaryOverlayDuration(Context context) {
        return Integer.parseInt(getString(context.getString(R.string.preference_secondary_overlay_duration), () -> context.getString(R.string.default_preference_secondary_overlay_duration)));
    }

    /**
     * Get the secondary overlay theme.
     *
     * @param context Ki2 application context. Cannot be a context generated from another package.
     * @return Secondary overlay theme.
     */
    public String getSecondaryOverlayTheme(Context context) {
        return getString(context.getString(R.string.preference_secondary_overlay_theme), () -> context.getString(R.string.default_preference_secondary_overlay_theme));
    }

    /**
     * Get the secondary overlay opacity.
     *
     * @param context Ki2 application context. Cannot be a context generated from another package.
     * @return Secondary overlay opacity.
     */
    public float getSecondaryOverlayOpacity(Context context) {
        float opacity = getFloat(context.getString(R.string.preference_secondary_overlay_opacity), () -> (float) context.getResources().getInteger(R.integer.default_preference_secondary_overlay_opacity));
        return Math.min(1, Math.max(opacity, 0));
    }

    /**
     * Get the secondary overlay X position.
     *
     * @param context Ki2 application context. Cannot be a context generated from another package.
     * @return Secondary overlay X position.
     */
    public int getSecondaryOverlayPositionX(Context context) {
        return getInt(context.getString(R.string.preference_secondary_overlay_position_x), () -> {
            OverlayViewBuilderEntry overlayBuilder = OverlayViewBuilderRegistry.getBuilder(getSecondaryOverlayTheme(context));

            if (overlayBuilder != null) {
                return overlayBuilder.getDefaultPositionX();
            }

            return context.getResources().getInteger(R.integer.default_preference_secondary_overlay_position);
        });
    }

    /**
     * Get the secondary overlay Y position.
     *
     * @param context Ki2 application context. Cannot be a context generated from another package.
     * @return Secondary overlay Y position.
     */
    public int getSecondaryOverlayPositionY(Context context) {
        return getInt(context.getString(R.string.preference_secondary_overlay_position_y), () -> {
            OverlayViewBuilderEntry overlayBuilder = OverlayViewBuilderRegistry.getBuilder(getSecondaryOverlayTheme(context));

            if (overlayBuilder != null) {
                return overlayBuilder.getDefaultPositionY();
            }

            return context.getResources().getInteger(R.integer.default_preference_secondary_overlay_position);
        });
    }

    /**
     * Get the accent color in raw string format.
     *
     * @param context Ki2 application context. Cannot be a context generated from another package.
     * @return Accent color in raw string format.
     */
    public String getAccentColorRaw(Context context) {
        return getString(context.getString(R.string.preference_accent_color), () ->
                context.getResources().getString(R.string.default_preference_accent_color));
    }

    /**
     * Get the accent color value.
     *
     * @param context Ki2 application context. Cannot be a context generated from another package.
     * @param karooTheme Karoo theme.
     * @return Accent color.
     */
    @ColorInt
    public int getAccentColor(Context context, KarooTheme karooTheme) {
        return getOrCache(cachedAccentColor, () -> {
            String colorString = getString(context.getString(R.string.preference_accent_color), () ->
                    context.getResources().getString(R.string.default_preference_accent_color));

            switch (colorString) {
                case "default": return karooTheme == KarooTheme.WHITE ? context.getColor(R.color.hh_gears_active_light) : context.getColor(R.color.hh_gears_active_dark);
                case "blue": return context.getColor(R.color.hh_gears_blue);
                case "red": return context.getColor(R.color.hh_gears_red);
                case "green": return context.getColor(R.color.hh_gears_green);
                case "yellow": return context.getColor(R.color.hh_gears_yellow);
                case "orange": return context.getColor(R.color.hh_orange);
                case "pink": return context.getColor(R.color.pink);
            }

            return karooTheme == KarooTheme.WHITE ? context.getColor(R.color.hh_gears_active_light) : context.getColor(R.color.hh_gears_active_dark);
        }, value -> cachedAccentColor = value);
    }

}
