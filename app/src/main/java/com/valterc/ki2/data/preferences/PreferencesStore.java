package com.valterc.ki2.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import timber.log.Timber;

public class PreferencesStore {

    @SuppressWarnings("FieldCanBeLocal")
    private final SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChanged
            = this::onSharedPreferenceChanged;

    private final Consumer<PreferencesView> preferencesListener;
    private PreferencesView preferencesView;

    public PreferencesStore(Context context, Consumer<PreferencesView> preferencesListener) {
        this.preferencesListener = preferencesListener;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferencesView = new PreferencesView(preferences);
        preferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChanged);
    }

    private void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        preferencesView = new PreferencesView(sharedPreferences);
        preferencesListener.accept(preferencesView);
    }

    public PreferencesView getPreferences() {
        return preferencesView;
    }

}
