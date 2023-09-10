package com.valterc.ki2.fragments.settings.overlay.position;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.valterc.ki2.R;
import com.valterc.ki2.data.preferences.PreferencesView;

public class PrimaryOverlayPositionPreference extends PositionPreference {

    public PrimaryOverlayPositionPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public PrimaryOverlayPositionPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PrimaryOverlayPositionPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PrimaryOverlayPositionPreference(@NonNull Context context) {
        super(context);
    }

    @Override
    public void setValue(int x, int y) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(getContext().getString(R.string.preference_overlay_position_x), x);
        editor.putInt(getContext().getString(R.string.preference_overlay_position_y), y);
        editor.apply();
        notifyChanged();
    }

    @Override
    public String getOverlayTheme(){
        PreferencesView preferencesView = new PreferencesView(getContext());
        return preferencesView.getOverlayTheme(getContext());
    }

    @Override
    public int getPositionX() {
        PreferencesView preferencesView = new PreferencesView(getContext());
        return preferencesView.getOverlayPositionX(getContext());
    }

    @Override
    public int getPositionY() {
        PreferencesView preferencesView = new PreferencesView(getContext());
        return preferencesView.getOverlayPositionY(getContext());
    }
}
