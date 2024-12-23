package com.valterc.ki2.karoo.overlay.manager;

import android.view.ViewGroup;

import com.valterc.ki2.karoo.extension.Ki2ExtensionContext;

/** @noinspection FieldCanBeLocal*/
public class OverlayManager {

    private final BaseOverlayManager primaryOverlayManager;
    private final BaseOverlayManager secondaryOverlayManager;

    public OverlayManager(Ki2ExtensionContext extensionContext) {
        primaryOverlayManager = new PrimaryOverlayManager(extensionContext);
        secondaryOverlayManager = new SecondaryOverlayManager(extensionContext);

        primaryOverlayManager.setVisibilityListener(visible -> {
            if (secondaryOverlayManager.getOverlayTriggers().isTriggeredByPrimaryHidden()) {
                if (visible) {
                    secondaryOverlayManager.hideOverlay();
                } else {
                    secondaryOverlayManager.showOverlay(true);
                }
            }
        });
    }
}
