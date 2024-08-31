package com.valterc.ki2.fragments.settings.graphics.gear;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.valterc.ki2.R;
import com.valterc.ki2.views.GearsView;

public class AccentColorViewHolder extends RecyclerView.ViewHolder {

    private final View baseView;
    private final GearsView gearsView;
    private final TextView textViewName;

    public AccentColorViewHolder(@NonNull View itemView) {
        super(itemView);

        baseView = itemView;
        gearsView = itemView.findViewById(R.id.gearsview_accent_color);
        textViewName = itemView.findViewById(R.id.textview_accent_color_name);
    }

    public View getRootView() {
        return baseView;
    }

    public GearsView getGearsView() {
        return gearsView;
    }

    public TextView getTextViewName() {
        return textViewName;
    }
}