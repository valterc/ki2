package com.valterc.ki2.karoo.overlay.manager;

import com.valterc.ki2.karoo.Ki2ExtensionContext;

/**
 * @noinspection FieldCanBeLocal
 */
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

    public void dispose() {
        primaryOverlayManager.dispose();
        secondaryOverlayManager.dispose();
    }

    public void refreshOverlays() {
        primaryOverlayManager.refreshOverlay();
        secondaryOverlayManager.refreshOverlay();
    }
}
