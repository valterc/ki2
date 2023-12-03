package com.valterc.ki2.karoo.overlay.manager;

import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.karoo.handlers.IRideHandler;

/** @noinspection FieldCanBeLocal*/
public class OverlayManager implements IRideHandler {

    private final BaseOverlayManager primaryOverlayManager;
    private final BaseOverlayManager secondaryOverlayManager;

    public OverlayManager(Ki2Context ki2Context) {
        primaryOverlayManager = new PrimaryOverlayManager(ki2Context);
        secondaryOverlayManager = new SecondaryOverlayManager(ki2Context);

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
