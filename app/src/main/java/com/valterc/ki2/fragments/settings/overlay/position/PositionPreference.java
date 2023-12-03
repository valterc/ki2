package com.valterc.ki2.fragments.settings.overlay.position;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;

import java.util.Locale;

@SuppressWarnings("unused")
public abstract class PositionPreference extends DialogPreference {

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

        if (originalSummary == null) {
            return String.format(Locale.getDefault(), "%d - %d", getPositionX(), getPositionY());
        }

        SpannableString spannableString = new SpannableString(originalSummary + "\n" +
                String.format(Locale.getDefault(), "%d - %d", getPositionX(), getPositionY()));
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), originalSummary.length(), spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    public abstract void setValue(int x, int y);

    public abstract String getOverlayTheme();

    public abstract int getPositionX();

    public abstract int getPositionY();

}
