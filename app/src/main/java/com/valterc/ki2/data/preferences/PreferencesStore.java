package com.valterc.ki2.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import java.util.function.Consumer;

import timber.log.Timber;

public class PreferencesStore {

    private final Consumer<PreferencesView> preferenceListener;
    private PreferencesView preferencesView;

    public PreferencesStore(Context context, Consumer<PreferencesView> preferenceListener) {
        this.preferenceListener = preferenceListener;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferencesView = new PreferencesView(preferences);
        preferences.registerOnSharedPreferenceChangeListener(this::onSharedPreferenceChanged);
    }

    private void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        preferencesView = new PreferencesView(sharedPreferences);
        preferenceListener.accept(preferencesView);
    }

    public PreferencesView getPreferences() {
        return preferencesView;
    }

}
