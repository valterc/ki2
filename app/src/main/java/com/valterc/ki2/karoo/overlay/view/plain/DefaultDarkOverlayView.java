package com.valterc.ki2.karoo.overlay.view.plain;

import android.view.View;

import com.valterc.ki2.R;
import com.valterc.ki2.karoo.Ki2Context;

public class DefaultDarkOverlayView extends DefaultOverlayView{

    public DefaultDarkOverlayView(Ki2Context context, View view) {
        super(context, view);

        getViewHolder().getOverlayView().setBackgroundResource(R.drawable.background_overlay_dark);
        getViewHolder().getLinearLayoutTopBar().setBackgroundResource(R.drawable.background_overlay_dark_top);

        getViewHolder().getTextViewDeviceName().setTextColor(getContext().getColor(android.R.color.primary_text_dark));
        getViewHolder().getTextViewBattery().setTextColor(getContext().getColor(android.R.color.primary_text_dark));

        getViewHolder().getGearsView().setUnselectedGearBorderColor(getContext().getColor(R.color.hh_gears_border_white));

        getViewHolder().getTextViewGearing().setTextColor(getContext().getColor(android.R.color.primary_text_dark));
        getViewHolder().getTextViewGearingExtra().setTextColor(getContext().getColor(android.R.color.primary_text_dark));
    }

}
