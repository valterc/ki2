package com.valterc.ki2.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceViewHolder;

@SuppressWarnings("unused")
public class MultiLinePreferenceCategory extends PreferenceCategory {

    public MultiLinePreferenceCategory(Context ctx, AttributeSet attrs, int defStyle) {
        super(ctx, attrs, defStyle);
    }

    public MultiLinePreferenceCategory(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        TextView summary = (TextView) holder.findViewById(android.R.id.summary);
        if (summary != null) {
            summary.setSingleLine(false);
            summary.setMaxLines(10);
        }
    }
}
