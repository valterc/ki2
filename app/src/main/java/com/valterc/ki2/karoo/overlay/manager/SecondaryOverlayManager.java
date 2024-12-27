package com.valterc.ki2.karoo.overlay.manager;

import com.valterc.ki2.karoo.Ki2ExtensionContext;
import com.valterc.ki2.karoo.overlay.OverlayPreferences;

import java.util.Set;

public class SecondaryOverlayManager extends BaseOverlayManager {

    public SecondaryOverlayManager(Ki2ExtensionContext extensionContext) {
        super(extensionContext);
    }

    @Override
    protected Set<String> getOverlayTriggersSet() {
        return getPreferences().getSecondaryOverlayTriggers(getExtensionContext().getContext());
    }

    @Override
    protected int getOverlayDuration() {
        return getPreferences().getSecondaryOverlayDuration(getExtensionContext().getContext());
    }

    @Override
    protected boolean isOverlayEnabled() {
        return getPreferences().isSecondaryOverlayEnabled(getExtensionContext().getContext());
    }

    @Override
    protected int getOverlayPositionY() {
        return getPreferences().getSecondaryOverlayPositionY(getExtensionContext().getContext());
    }

    @Override
    protected int getOverlayPositionX() {
        return getPreferences().getSecondaryOverlayPositionX(getExtensionContext().getContext());
    }

    @Override
    protected String getOverlayTheme() {
        return getPreferences().getSecondaryOverlayTheme(getExtensionContext().getContext());
    }

    @Override
    protected OverlayPreferences getOverlayPreferences() {
        return new OverlayPreferences(
                getPreferences().isSecondaryOverlayEnabled(getExtensionContext().getContext()),
                getPreferences().getSecondaryOverlayTheme(getExtensionContext().getContext()),
                getPreferences().getSecondaryOverlayDuration(getExtensionContext().getContext()),
                getPreferences().getSecondaryOverlayOpacity(getExtensionContext().getContext()),
                getPreferences().getSecondaryOverlayPositionX(getExtensionContext().getContext()),
                getPreferences().getSecondaryOverlayPositionY(getExtensionContext().getContext())
        );
    }
}
