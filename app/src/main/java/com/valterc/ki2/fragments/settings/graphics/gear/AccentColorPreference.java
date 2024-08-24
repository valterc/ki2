package com.valterc.ki2.fragments.settings.graphics.gear;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceManager;

import com.valterc.ki2.R;
import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.karoo.views.KarooTheme;

@SuppressWarnings("unused")
public class AccentColorPreference extends DialogPreference {

    public AccentColorPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public AccentColorPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AccentColorPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AccentColorPreference(@NonNull Context context) {
        super(context);
    }

    @Nullable
    @Override
    public CharSequence getSummary() {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(super.getSummary());
        spannableStringBuilder.append("\n");
        spannableStringBuilder.append("     ", new BackgroundColorSpan(getColor()), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        String colorTitle = getColorTitle();
        if (colorTitle == null) {
            spannableStringBuilder.append(String.format(" - #%06X ", (0xFFFFFF & getColor())), new ForegroundColorSpan(getColor()), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            spannableStringBuilder.append(" - " + colorTitle, new ForegroundColorSpan(getColor()), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spannableStringBuilder;
    }

    public void setValue(String color) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getContext().getString(R.string.preference_accent_color), color);
        editor.apply();
        notifyChanged();
    }

    private int getColor() {
        PreferencesView preferencesView = new PreferencesView(getContext());
        return preferencesView.getAccentColor(getContext(), KarooTheme.UNKNOWN);
    }

    @Nullable
    private String getColorTitle(){
        String[] colorValues = getContext().getResources().getStringArray(R.array.preference_values_accent_color);
        String[] colorTitles = getContext().getResources().getStringArray(R.array.preference_titles_accent_color);

        PreferencesView preferencesView = new PreferencesView(getContext());
        String colorValue = preferencesView.getAccentColorRaw(getContext());

        for (int i = 0; i < colorValues.length; i++) {
            if (colorValue.equals(colorValues[i])){
                return colorTitles[i];
            }
        }

        return null;
    }

}
