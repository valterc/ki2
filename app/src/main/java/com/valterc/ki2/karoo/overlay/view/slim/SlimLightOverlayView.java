package com.valterc.ki2.karoo.overlay.view.slim;

import android.view.View;

import com.valterc.ki2.R;
import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.karoo.Ki2Context;

public class SlimLightOverlayView extends SlimOverlayView {

    public SlimLightOverlayView(Ki2Context ki2Context, PreferencesView preferences, View view) {
        super(ki2Context, preferences, view);

        getViewHolder().getOverlayView().setBackgroundColor(getContext().getColor(R.color.hh_black_dark));
        getViewHolder().getSlimGearsView().setGearColor(getContext().getColor(R.color.white));
    }

}
