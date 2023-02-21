package com.valterc.ki2.karoo.overlay.view.plain;

import android.content.Context;
import android.view.View;

import com.valterc.ki2.R;

public class DefaultLightOverlayView extends DefaultOverlayView{

    public DefaultLightOverlayView(Context context, View view) {
        super(context, view);

        getViewHolder().getOverlayView().setBackgroundResource(R.drawable.background_overlay_light);
        getViewHolder().getLinearLayoutTopBar().setBackgroundResource(R.drawable.background_overlay_light_top);

        getViewHolder().getTextViewDeviceName().setTextColor(getContext().getColor(android.R.color.primary_text_light));
        getViewHolder().getTextViewBattery().setTextColor(getContext().getColor(android.R.color.primary_text_light));

        getViewHolder().getGearsView().setUnselectedGearBorderColor(getContext().getColor(R.color.hh_gears_border_dark));

        getViewHolder().getTextViewGearing().setTextColor(getContext().getColor(android.R.color.primary_text_light));
        getViewHolder().getTextViewGearingExtra().setTextColor(getContext().getColor(android.R.color.primary_text_light));
    }

}
