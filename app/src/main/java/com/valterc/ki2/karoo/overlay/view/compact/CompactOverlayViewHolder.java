package com.valterc.ki2.karoo.overlay.view.compact;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.valterc.ki2.R;
import com.valterc.ki2.karoo.overlay.view.BaseOverlayViewHolder;
import com.valterc.ki2.views.GearsView;
import com.valterc.ki2.views.battery.BatteryView;

public class CompactOverlayViewHolder extends BaseOverlayViewHolder {


    private final TextView textViewGearing;
    private final TextView textViewGearingExtra;
    private final LinearLayout linearLayoutGearingRatio;
    private final TextView textViewGearingRatio;

    public CompactOverlayViewHolder(@NonNull View overlayView) {
        super(overlayView);

        this.textViewGearing = overlayView.findViewById(R.id.textview_karoo_overlay_compact_gearing);
        this.textViewGearingExtra = overlayView.findViewById(R.id.textview_karoo_overlay_compact_gearing_extra);
        this.linearLayoutGearingRatio = overlayView.findViewById(R.id.linearlayout_karoo_overlay_compact_gearing_ratio);
        this.textViewGearingRatio = overlayView.findViewById(R.id.textview_karoo_overlay_compact_gearing_ratio);
    }



    public TextView getTextViewGearing() {
        return textViewGearing;
    }

    public TextView getTextViewGearingExtra() {
        return textViewGearingExtra;
    }

    public LinearLayout getLinearLayoutGearingRatio() {
        return linearLayoutGearingRatio;
    }

    public TextView getTextViewGearingRatio() {
        return textViewGearingRatio;
    }

}
