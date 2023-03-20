package com.valterc.ki2.karoo.overlay.view.plain;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.valterc.ki2.R;
import com.valterc.ki2.karoo.overlay.view.BaseOverlayViewHolder;
import com.valterc.ki2.views.GearsView;
import com.valterc.ki2.views.battery.BatteryView;

public class DefaultOverlayViewHolder extends BaseOverlayViewHolder {

    private final LinearLayout linearLayoutTopBar;
    private final TextView textViewDeviceName;
    private final BatteryView batteryView;
    private final TextView textViewBattery;
    private final GearsView gearsView;
    private final TextView textViewGearing;
    private final TextView textViewGearingExtra;
    private final LinearLayout linearLayoutGearingRatio;
    private final TextView textViewGearingRatio;

    public DefaultOverlayViewHolder(@NonNull View overlayView) {
        super(overlayView);

        this.linearLayoutTopBar = overlayView.findViewById(R.id.linearlayout_karoo_overlay_top_bar);
        this.textViewDeviceName = overlayView.findViewById(R.id.textView_karoo_overlay_device_name);
        this.batteryView = overlayView.findViewById(R.id.batteryview_karoo_overlay_battery);
        this.textViewBattery = overlayView.findViewById(R.id.textview_karoo_overlay_battery);
        this.gearsView = overlayView.findViewById(R.id.gearsview_karoo_overlay_gearing);
        this.textViewGearing = overlayView.findViewById(R.id.textview_karoo_overlay_gearing);
        this.textViewGearingExtra = overlayView.findViewById(R.id.textview_karoo_overlay_gearing_extra);
        this.linearLayoutGearingRatio = overlayView.findViewById(R.id.linearlayout_karoo_overlay_gearing_extra);
        this.textViewGearingRatio = overlayView.findViewById(R.id.textview_karoo_overlay_gearing_ratio);
    }

    public LinearLayout getLinearLayoutTopBar() {
        return linearLayoutTopBar;
    }

    public TextView getTextViewDeviceName() {
        return textViewDeviceName;
    }

    public BatteryView getBatteryView() {
        return batteryView;
    }

    public TextView getTextViewBattery() {
        return textViewBattery;
    }

    public GearsView getGearsView() {
        return gearsView;
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
