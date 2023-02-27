package com.valterc.ki2.fragments.settings.overlay;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.valterc.ki2.R;

public class OverlayThemeViewHolder extends RecyclerView.ViewHolder {

    private final View baseView;
    private final LinearLayout linearLayoutViewContainer;
    private final TextView textViewName;

    public OverlayThemeViewHolder(@NonNull View itemView) {
        super(itemView);

        baseView = itemView;
        linearLayoutViewContainer = itemView.findViewById(R.id.viewstub_overlay_theme_view_container);
        textViewName = itemView.findViewById(R.id.textview_overlay_theme_name);
    }

    public View getRootView(){
        return baseView;
    }

    public LinearLayout getLinearLayoutViewContainer() {
        return linearLayoutViewContainer;
    }

    public TextView getTextViewName() {
        return textViewName;
    }
}