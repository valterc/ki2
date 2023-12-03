package com.valterc.ki2.fragments.settings.overlay.opacity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceManager;

import com.valterc.ki2.R;
import com.valterc.ki2.data.preferences.PreferencesView;

import java.util.Locale;

@SuppressWarnings("unused")
public class OpacityPreference extends DialogPreference {

    public OpacityPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public OpacityPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public OpacityPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public OpacityPreference(@NonNull Context context) {
        super(context);
    }

    @Nullable
    @Override
    public CharSequence getSummary() {
        CharSequence originalSummary = super.getSummary();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        if (originalSummary == null) {
            return String.format(Locale.getDefault(), "%.0f", sharedPreferences.getFloat(getKey(), 1) * 100) + "%";
        }

        SpannableString spannableString = new SpannableString(originalSummary + "\n" +
                String.format(Locale.getDefault(), "%.0f", sharedPreferences.getFloat(getKey(), 1) * 100) + "%");
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), originalSummary.length(), spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    public void setValue(float value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(getKey(), value);
        editor.apply();
        notifyChanged();
    }

}
