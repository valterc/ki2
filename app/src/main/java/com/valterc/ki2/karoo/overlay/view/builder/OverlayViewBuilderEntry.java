package com.valterc.ki2.karoo.overlay.view.builder;

import android.util.Pair;
import android.view.View;

import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.karoo.overlay.view.IOverlayView;

import java.util.function.BiFunction;

public class OverlayViewBuilderEntry {

    private final int layoutId;
    private final BiFunction<Ki2Context, View, IOverlayView> builder;
    private final int defaultPositionX;
    private final int defaultPositionY;

    public OverlayViewBuilderEntry(int layoutId, int defaultPositionX, int defaultPositionY, BiFunction<Ki2Context, View, IOverlayView> builder) {
        this.layoutId = layoutId;
        this.defaultPositionX = defaultPositionX;
        this.defaultPositionY = defaultPositionY;
        this.builder = builder;
    }

    public OverlayViewBuilderEntry(int layoutId, Pair<Integer, Integer> defaultPosition, BiFunction<Ki2Context, View, IOverlayView> builder) {
        this(layoutId, defaultPosition.first, defaultPosition.second, builder);
    }

    public int getLayoutId() {
        return layoutId;
    }

    public IOverlayView createOverlayView(Ki2Context ki2Context, View view) {
        return builder.apply(ki2Context, view);
    }

    public int getDefaultPositionX() {
        return defaultPositionX;
    }

    public int getDefaultPositionY() {
        return defaultPositionY;
    }
}
