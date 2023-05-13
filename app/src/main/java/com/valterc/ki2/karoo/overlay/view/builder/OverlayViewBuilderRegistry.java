package com.valterc.ki2.karoo.overlay.view.builder;

import android.util.Pair;

import androidx.annotation.Nullable;

import com.valterc.ki2.R;
import com.valterc.ki2.karoo.overlay.view.compact.CompactDarkGearIndexOverlayView;
import com.valterc.ki2.karoo.overlay.view.compact.CompactDarkGearSizeOverlayView;
import com.valterc.ki2.karoo.overlay.view.compact.CompactDarkGearSizeRatioOverlayView;
import com.valterc.ki2.karoo.overlay.view.compact.CompactDarkRearGearIndexOverlayView;
import com.valterc.ki2.karoo.overlay.view.compact.CompactDarkRearGearSizeOverlayView;
import com.valterc.ki2.karoo.overlay.view.compact.CompactDarkRearGearSizeRatioOverlayView;
import com.valterc.ki2.karoo.overlay.view.compact.CompactLightGearIndexOverlayView;
import com.valterc.ki2.karoo.overlay.view.compact.CompactLightGearSizeOverlayView;
import com.valterc.ki2.karoo.overlay.view.compact.CompactLightGearSizeRatioOverlayView;
import com.valterc.ki2.karoo.overlay.view.compact.CompactLightRearGearIndexOverlayView;
import com.valterc.ki2.karoo.overlay.view.compact.CompactLightRearGearSizeOverlayView;
import com.valterc.ki2.karoo.overlay.view.compact.CompactLightRearGearSizeRatioOverlayView;
import com.valterc.ki2.karoo.overlay.view.plain.DefaultDarkGearIndexOverlayView;
import com.valterc.ki2.karoo.overlay.view.plain.DefaultDarkGearSizeOverlayView;
import com.valterc.ki2.karoo.overlay.view.plain.DefaultDarkGearSizeRatioOverlayView;
import com.valterc.ki2.karoo.overlay.view.plain.DefaultLightGearIndexOverlayView;
import com.valterc.ki2.karoo.overlay.view.plain.DefaultLightGearSizeOverlayView;
import com.valterc.ki2.karoo.overlay.view.plain.DefaultLightGearSizeRatioOverlayView;
import com.valterc.ki2.karoo.overlay.view.simple.SimpleDarkOverlayView;
import com.valterc.ki2.karoo.overlay.view.simple.SimpleLightOverlayView;

import java.util.HashMap;

public final class OverlayViewBuilderRegistry {

    private OverlayViewBuilderRegistry() {
    }

    private static final HashMap<String, OverlayViewBuilderEntry> builderMap = new HashMap<>();

    static {
        builderMap.put("default_dark_gear_index", new OverlayViewBuilderEntry(R.layout.view_karoo_overlay, new Pair<>(0, 5), DefaultDarkGearIndexOverlayView::new));
        builderMap.put("default_light_gear_index", new OverlayViewBuilderEntry(R.layout.view_karoo_overlay, new Pair<>(0, 5), DefaultLightGearIndexOverlayView::new));

        builderMap.put("default_dark_gear_size", new OverlayViewBuilderEntry(R.layout.view_karoo_overlay, new Pair<>(0, 5), DefaultDarkGearSizeOverlayView::new));
        builderMap.put("default_light_gear_size", new OverlayViewBuilderEntry(R.layout.view_karoo_overlay, new Pair<>(0, 5), DefaultLightGearSizeOverlayView::new));

        builderMap.put("default_dark_gear_size_ratio", new OverlayViewBuilderEntry(R.layout.view_karoo_overlay, new Pair<>(0, 5), DefaultDarkGearSizeRatioOverlayView::new));
        builderMap.put("default_light_gear_size_ratio", new OverlayViewBuilderEntry(R.layout.view_karoo_overlay, new Pair<>(0, 5), DefaultLightGearSizeRatioOverlayView::new));

        builderMap.put("simple_dark", new OverlayViewBuilderEntry(R.layout.view_karoo_overlay, new Pair<>(0, 5), SimpleDarkOverlayView::new));
        builderMap.put("simple_light", new OverlayViewBuilderEntry(R.layout.view_karoo_overlay, new Pair<>(0, 5), SimpleLightOverlayView::new));

        builderMap.put("compact_dark_gear_index", new OverlayViewBuilderEntry(R.layout.view_karoo_overlay_compact, new Pair<>(5, 65), CompactDarkGearIndexOverlayView::new));
        builderMap.put("compact_light_gear_index", new OverlayViewBuilderEntry(R.layout.view_karoo_overlay_compact, new Pair<>(5, 65), CompactLightGearIndexOverlayView::new));

        builderMap.put("compact_dark_gear_size", new OverlayViewBuilderEntry(R.layout.view_karoo_overlay_compact, new Pair<>(5, 65), CompactDarkGearSizeOverlayView::new));
        builderMap.put("compact_light_gear_size", new OverlayViewBuilderEntry(R.layout.view_karoo_overlay_compact, new Pair<>(5, 65), CompactLightGearSizeOverlayView::new));

        builderMap.put("compact_dark_gear_size_ratio", new OverlayViewBuilderEntry(R.layout.view_karoo_overlay_compact, new Pair<>(5, 65), CompactDarkGearSizeRatioOverlayView::new));
        builderMap.put("compact_light_gear_size_ratio", new OverlayViewBuilderEntry(R.layout.view_karoo_overlay_compact, new Pair<>(5, 65), CompactLightGearSizeRatioOverlayView::new));

        builderMap.put("compact_dark_rear_gear_index", new OverlayViewBuilderEntry(R.layout.view_karoo_overlay_compact, new Pair<>(5, 65), CompactDarkRearGearIndexOverlayView::new));
        builderMap.put("compact_light_rear_gear_index", new OverlayViewBuilderEntry(R.layout.view_karoo_overlay_compact, new Pair<>(5, 65), CompactLightRearGearIndexOverlayView::new));

        builderMap.put("compact_dark_rear_gear_size", new OverlayViewBuilderEntry(R.layout.view_karoo_overlay_compact, new Pair<>(5, 65), CompactDarkRearGearSizeOverlayView::new));
        builderMap.put("compact_light_rear_gear_size", new OverlayViewBuilderEntry(R.layout.view_karoo_overlay_compact, new Pair<>(5, 65), CompactLightRearGearSizeOverlayView::new));

        builderMap.put("compact_dark_rear_gear_size_ratio", new OverlayViewBuilderEntry(R.layout.view_karoo_overlay_compact, new Pair<>(5, 65), CompactDarkRearGearSizeRatioOverlayView::new));
        builderMap.put("compact_light_rear_gear_size_ratio", new OverlayViewBuilderEntry(R.layout.view_karoo_overlay_compact, new Pair<>(5, 65), CompactLightRearGearSizeRatioOverlayView::new));
    }

    @Nullable
    public static OverlayViewBuilderEntry getBuilder(String key) {
        return builderMap.get(key);
    }

}
