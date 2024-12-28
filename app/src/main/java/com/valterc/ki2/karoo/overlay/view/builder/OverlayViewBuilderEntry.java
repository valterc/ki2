package com.valterc.ki2.karoo.overlay.view.builder;

import android.util.Pair;
import android.view.View;

import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.karoo.Ki2ExtensionContext;
import com.valterc.ki2.karoo.overlay.view.IOverlayView;

import kotlin.jvm.functions.Function3;

public class OverlayViewBuilderEntry {

    private final int layoutId;
    private final Function3<Ki2ExtensionContext, PreferencesView, View, IOverlayView> builder;
    private final int defaultPositionX;
    private final int defaultPositionY;

    public OverlayViewBuilderEntry(int layoutId, int defaultPositionX, int defaultPositionY, Function3<Ki2ExtensionContext, PreferencesView, View, IOverlayView> builder) {
        this.layoutId = layoutId;
        this.defaultPositionX = defaultPositionX;
        this.defaultPositionY = defaultPositionY;
        this.builder = builder;
    }

    public OverlayViewBuilderEntry(int layoutId, Pair<Integer, Integer> defaultPosition, Function3<Ki2ExtensionContext, PreferencesView, View, IOverlayView> builder) {
        this(layoutId, defaultPosition.first, defaultPosition.second, builder);
    }

    public int getLayoutId() {
        return layoutId;
    }

    public IOverlayView createOverlayView(Ki2ExtensionContext extensionContext, PreferencesView preferencesView, View view) {
        return builder.invoke(extensionContext, preferencesView, view);
    }

    public int getDefaultPositionX() {
        return defaultPositionX;
    }

    public int getDefaultPositionY() {
        return defaultPositionY;
    }
}
