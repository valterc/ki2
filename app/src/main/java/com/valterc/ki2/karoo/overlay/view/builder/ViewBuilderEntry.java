package com.valterc.ki2.karoo.overlay.view.builder;

import android.view.View;

import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.karoo.overlay.view.IOverlayView;

import java.util.function.BiFunction;

public class ViewBuilderEntry {

    private final int layoutId;

    private final BiFunction<Ki2Context, View, IOverlayView> builder;

    public ViewBuilderEntry(int layoutId, BiFunction<Ki2Context, View, IOverlayView> builder) {
        this.layoutId = layoutId;
        this.builder = builder;
    }

    public int getLayoutId() {
        return layoutId;
    }

    public IOverlayView createOverlayView(Ki2Context ki2Context, View view) {
        return builder.apply(ki2Context, view);
    }
}
