package com.valterc.ki2.karoo.overlay.view.slim;

import android.view.View;

import com.valterc.ki2.R;
import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.karoo.Ki2ExtensionContext;

public class SlimDarkOverlayView extends SlimOverlayView {

    public SlimDarkOverlayView(Ki2ExtensionContext ki2Context, PreferencesView preferences, View view) {
        super(ki2Context, preferences, view);

        getViewHolder().getOverlayView().setBackgroundColor(getContext().getColor(R.color.white_100));
        getViewHolder().getSlimGearsView().setGearColor(getContext().getColor(R.color.black));
    }

}
