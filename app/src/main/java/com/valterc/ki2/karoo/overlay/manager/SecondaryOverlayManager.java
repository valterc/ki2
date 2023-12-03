package com.valterc.ki2.karoo.overlay.manager;

import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.karoo.overlay.OverlayPreferences;

import java.util.Set;

public class SecondaryOverlayManager extends BaseOverlayManager {

    public SecondaryOverlayManager(Ki2Context ki2Context) {
        super(ki2Context);
    }

    @Override
    protected Set<String> getOverlayTriggersSet() {
        return getPreferences().getSecondaryOverlayTriggers(getKi2Context().getSdkContext());
    }

    @Override
    protected int getOverlayDuration() {
        return getPreferences().getSecondaryOverlayDuration(getKi2Context().getSdkContext());
    }

    @Override
    protected boolean isOverlayEnabled() {
        return getPreferences().isSecondaryOverlayEnabled(getKi2Context().getSdkContext());
    }

    @Override
    protected int getOverlayPositionY() {
        return getPreferences().getSecondaryOverlayPositionY(getKi2Context().getSdkContext());
    }

    @Override
    protected int getOverlayPositionX() {
        return getPreferences().getSecondaryOverlayPositionX(getKi2Context().getSdkContext());
    }

    @Override
    protected String getOverlayTheme() {
        return getPreferences().getSecondaryOverlayTheme(getKi2Context().getSdkContext());
    }

    @Override
    protected OverlayPreferences getOverlayPreferences() {
        return new OverlayPreferences(
                getPreferences().isSecondaryOverlayEnabled(getKi2Context().getSdkContext()),
                getPreferences().getSecondaryOverlayTheme(getKi2Context().getSdkContext()),
                getPreferences().getSecondaryOverlayDuration(getKi2Context().getSdkContext()),
                getPreferences().getSecondaryOverlayOpacity(getKi2Context().getSdkContext()),
                getPreferences().getSecondaryOverlayPositionX(getKi2Context().getSdkContext()),
                getPreferences().getSecondaryOverlayPositionY(getKi2Context().getSdkContext())
        );
    }
}
