package com.valterc.ki2.fragments.settings.graphics.gear;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.valterc.ki2.R;
import com.valterc.ki2.views.GearsView;

import org.w3c.dom.Text;

public class GearColorViewHolder extends RecyclerView.ViewHolder {

    private final View baseView;
    private final GearsView gearsView;
    private final TextView textViewName;

    public GearColorViewHolder(@NonNull View itemView) {
        super(itemView);

        baseView = itemView;
        gearsView = itemView.findViewById(R.id.gearsview_gear_color);
        textViewName = itemView.findViewById(R.id.textview_gear_color_name);
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