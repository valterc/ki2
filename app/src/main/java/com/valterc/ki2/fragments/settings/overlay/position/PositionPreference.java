package com.valterc.ki2.fragments.settings.overlay.position;

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
public class PositionPreference extends DialogPreference {

    public PositionPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public PositionPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PositionPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PositionPreference(@NonNull Context context) {
        super(context);
    }

    @Nullable
    @Override
    public CharSequence getSummary() {
        CharSequence originalSummary = super.getSummary();

        PreferencesView preferencesView = new PreferencesView(getContext());

        if (originalSummary == null) {
            return String.format(Locale.getDefault(), "%d - %d", preferencesView.getOverlayPositionX(getContext()), preferencesView.getOverlayPositionY(getContext()));
        }

        SpannableString spannableString = new SpannableString(originalSummary + "\n" +
                String.format(Locale.getDefault(), "%d - %d", preferencesView.getOverlayPositionX(getContext()), preferencesView.getOverlayPositionY(getContext())));
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), originalSummary.length(), spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    public void setValue(int x, int y) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(getContext().getString(R.string.preference_overlay_position_x), x);
        editor.putInt(getContext().getString(R.string.preference_overlay_position_y), y);
        editor.apply();
        notifyChanged();
    }

}
