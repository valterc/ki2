package com.valterc.ki2.views.preference;

import android.content.Context;
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
        return super.getSummary() + "\n" + getEntry();
    }

}
