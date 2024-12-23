package com.valterc.ki2.karoo.overlay.view.compact;

import android.view.View;

import com.valterc.ki2.R;
import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.karoo.extension.Ki2ExtensionContext;

public class CompactLightOverlayView extends CompactOverlayView {

    public CompactLightOverlayView(Ki2ExtensionContext context, PreferencesView preferences, View view) {
        super(context, preferences, view);

        getViewHolder().getOverlayView().setBackgroundResource(R.drawable.background_overlay_light);
        getViewHolder().getTextViewGearing().setTextColor(getContext().getColor(android.R.color.primary_text_light));
        getViewHolder().getTextViewGearingExtra().setTextColor(getContext().getColor(android.R.color.primary_text_light));
        getViewHolder().getTextViewGearingRatio().setTextColor(getContext().getColor(android.R.color.primary_text_light));
    }

}
