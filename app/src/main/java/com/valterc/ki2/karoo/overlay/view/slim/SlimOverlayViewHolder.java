package com.valterc.ki2.karoo.overlay.view.slim;

import android.view.View;

import androidx.annotation.NonNull;

import com.valterc.ki2.R;
import com.valterc.ki2.karoo.overlay.view.BaseOverlayViewHolder;
import com.valterc.ki2.views.SlimGearsView;

public class SlimOverlayViewHolder extends BaseOverlayViewHolder {

    private final SlimGearsView slimGearsView;

    public SlimOverlayViewHolder(@NonNull View overlayView) {
        super(overlayView);

        this.slimGearsView = overlayView.findViewById(R.id.slimgearsview_karoo_overlay_slim_gearing);
    }

    public SlimGearsView getSlimGearsView() {
        return slimGearsView;
    }

}
