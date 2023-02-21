package com.valterc.ki2.karoo.overlay.view.builder;

import android.content.Context;
import android.view.View;

import com.valterc.ki2.karoo.overlay.view.IOverlayView;

import java.util.function.BiFunction;

public class ViewBuilderEntry {

    private final int layoutId;

    private final BiFunction<Context, View, IOverlayView> builder;

    public ViewBuilderEntry(int layoutId, BiFunction<Context, View, IOverlayView> builder) {
        this.layoutId = layoutId;
        this.builder = builder;
    }

    public int getLayoutId() {
        return layoutId;
    }

    public IOverlayView createOverlayView(Context context, View view) {
        return builder.apply(context, view);
    }
}
