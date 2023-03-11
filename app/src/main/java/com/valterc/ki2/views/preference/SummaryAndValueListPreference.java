package com.valterc.ki2.views.preference;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;

@SuppressWarnings("unused")
public class SummaryAndValueListPreference extends ListPreference {
    public SummaryAndValueListPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public SummaryAndValueListPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SummaryAndValueListPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SummaryAndValueListPreference(@NonNull Context context) {
        super(context);
    }

    @Nullable
    @Override
    public CharSequence getSummary() {
        CharSequence originalSummary = super.getSummary();

        if (originalSummary == null) {
            return getEntry();
        }

        SpannableString spannableString = new SpannableString(originalSummary + "\n" + getEntry());
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), originalSummary.length(), spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

}
