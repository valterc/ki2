package com.valterc.ki2.karoo.overlay.view.plain;

import android.view.View;

import com.valterc.ki2.R;
import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.karoo.Ki2ExtensionContext;
import com.valterc.ki2.karoo.views.KarooTheme;

public class DefaultLightOverlayView extends DefaultOverlayView {

    public DefaultLightOverlayView(Ki2ExtensionContext context, PreferencesView preferences, View view) {
        super(context, preferences, view);

        getViewHolder().getOverlayView().setBackgroundResource(R.drawable.background_overlay_light);
        getViewHolder().getLinearLayoutTopBar().setBackgroundResource(R.drawable.background_overlay_light_top);

        getViewHolder().getTextViewDeviceName().setTextColor(getContext().getColor(android.R.color.primary_text_light));
        getViewHolder().getTextViewBattery().setTextColor(getContext().getColor(android.R.color.primary_text_light));

        getViewHolder().getBatteryView().setBorderColor(getContext().getColor(R.color.battery_border_light));
        getViewHolder().getBatteryView().setBackgroundColor(getContext().getColor(R.color.battery_background_light));

        getViewHolder().getGearsView().setUnselectedGearBorderColor(getContext().getColor(R.color.hh_gears_border_light));
        getViewHolder().getGearsView().setSelectedGearColor(new PreferencesView(getContext()).getAccentColor(getContext(), KarooTheme.WHITE));

        getViewHolder().getTextViewGearing().setTextColor(getContext().getColor(android.R.color.primary_text_light));
        getViewHolder().getTextViewGearingExtra().setTextColor(getContext().getColor(android.R.color.primary_text_light));
        getViewHolder().getTextViewGearingRatio().setTextColor(getContext().getColor(android.R.color.primary_text_light));
    }

    @Override
    protected int getBatteryBorderColor() {
        return getContext().getColor(R.color.battery_border_light);
    }

}
