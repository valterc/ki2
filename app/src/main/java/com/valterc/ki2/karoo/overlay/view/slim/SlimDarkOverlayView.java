package com.valterc.ki2.karoo.overlay.view.slim;

import android.view.View;

import com.valterc.ki2.R;
import com.valterc.ki2.karoo.Ki2Context;

public class SlimDarkOverlayView extends SlimOverlayView {

    public SlimDarkOverlayView(Ki2Context ki2Context, View view) {
        super(ki2Context, view);

        getViewHolder().getOverlayView().setBackgroundColor(getContext().getColor(R.color.white_100));
        getViewHolder().getSlimGearsView().setGearColor(getContext().getColor(R.color.black));
    }

}
