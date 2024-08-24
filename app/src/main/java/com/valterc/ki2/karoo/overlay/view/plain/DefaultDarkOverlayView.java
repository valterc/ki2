package com.valterc.ki2.karoo.overlay.view.plain;

import android.view.View;

import com.valterc.ki2.R;
import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.karoo.views.KarooTheme;

public class DefaultDarkOverlayView extends DefaultOverlayView {

    public DefaultDarkOverlayView(Ki2Context context, PreferencesView preferences, View view) {
        super(context, preferences, view);

        getViewHolder().getOverlayView().setBackgroundResource(R.drawable.background_overlay_dark);
        getViewHolder().getLinearLayoutTopBar().setBackgroundResource(R.drawable.background_overlay_dark_top);

        getViewHolder().getTextViewDeviceName().setTextColor(getContext().getColor(android.R.color.primary_text_dark));
        getViewHolder().getTextViewBattery().setTextColor(getContext().getColor(android.R.color.primary_text_dark));

        getViewHolder().getBatteryView().setBorderColor(getContext().getColor(R.color.battery_border_dark));
        getViewHolder().getBatteryView().setBackgroundColor(getContext().getColor(R.color.battery_background_dark));

        getViewHolder().getGearsView().setUnselectedGearBorderColor(getContext().getColor(R.color.hh_gears_border_dark));
        getViewHolder().getGearsView().setSelectedGearColor(preferences.getAccentColor(getContext(), KarooTheme.DARK));

        getViewHolder().getTextViewGearing().setTextColor(getContext().getColor(android.R.color.primary_text_dark));
        getViewHolder().getTextViewGearingExtra().setTextColor(getContext().getColor(android.R.color.primary_text_dark));
        getViewHolder().getTextViewGearingRatio().setTextColor(getContext().getColor(android.R.color.primary_text_dark));
    }

    @Override
    protected int getBatteryBorderColor() {
        return getContext().getColor(R.color.battery_border_dark);
    }

}
