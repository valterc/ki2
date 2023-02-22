package com.valterc.ki2.karoo.overlay.view.builder;

import com.valterc.ki2.R;
import com.valterc.ki2.karoo.overlay.view.plain.DefaultDarkOverlayView;
import com.valterc.ki2.karoo.overlay.view.plain.DefaultLightOverlayView;

import java.util.HashMap;

public final class ViewBuilderRegistry {

    private ViewBuilderRegistry() {
    }

    private static final HashMap<String, ViewBuilderEntry> builderMap = new HashMap<>();

    static {
        builderMap.put("default_light", new ViewBuilderEntry(R.layout.view_karoo_overlay, DefaultLightOverlayView::new));
        builderMap.put("default_dark", new ViewBuilderEntry(R.layout.view_karoo_overlay, DefaultDarkOverlayView::new));
    }

    public static ViewBuilderEntry getBuilder(String key) {
        return builderMap.get(key);
    }

}
