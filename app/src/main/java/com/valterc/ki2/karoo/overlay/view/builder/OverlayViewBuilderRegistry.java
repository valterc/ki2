package com.valterc.ki2.karoo.overlay.view.builder;

import com.valterc.ki2.R;
import com.valterc.ki2.karoo.overlay.view.plain.DefaultDarkGearIndexOverlayView;
import com.valterc.ki2.karoo.overlay.view.plain.DefaultDarkGearSizeRatioOverlayView;
import com.valterc.ki2.karoo.overlay.view.plain.DefaultLightGearIndexOverlayView;
import com.valterc.ki2.karoo.overlay.view.plain.DefaultLightGearSizeRatioOverlayView;

import java.util.HashMap;

public final class OverlayViewBuilderRegistry {

    private OverlayViewBuilderRegistry() {
    }

    private static final HashMap<String, OverlayViewBuilderEntry> builderMap = new HashMap<>();

    static {
        builderMap.put("default_light_gear_size_ratio", new OverlayViewBuilderEntry(R.layout.view_karoo_overlay, DefaultLightGearSizeRatioOverlayView::new));
        builderMap.put("default_dark_gear_size_ratio", new OverlayViewBuilderEntry(R.layout.view_karoo_overlay, DefaultDarkGearSizeRatioOverlayView::new));
        builderMap.put("default_light_gear_index", new OverlayViewBuilderEntry(R.layout.view_karoo_overlay, DefaultLightGearIndexOverlayView::new));
        builderMap.put("default_dark_gear_index", new OverlayViewBuilderEntry(R.layout.view_karoo_overlay, DefaultDarkGearIndexOverlayView::new));
    }

    public static OverlayViewBuilderEntry getBuilder(String key) {
        return builderMap.get(key);
    }

}
