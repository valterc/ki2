package com.valterc.ki2.karoo.overlay.manager;

import com.valterc.ki2.data.message.ShowOverlayMessage;
import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.karoo.overlay.OverlayPreferences;

import java.util.Set;
import java.util.function.Consumer;

public class PrimaryOverlayManager extends BaseOverlayManager {

    @SuppressWarnings("FieldCanBeLocal")
    private final Consumer<ShowOverlayMessage> showOverlayListener = (showOverlayMessage) -> showOverlay(true);

    public PrimaryOverlayManager(Ki2Context ki2Context) {
        super(ki2Context);

        ki2Context.getServiceClient().getCustomMessageClient().registerShowOverlayWeakListener(showOverlayListener);
    }

    @Override
    protected Set<String> getOverlayTriggersSet() {
        return getPreferences().getOverlayTriggers(getKi2Context().getSdkContext());
    }

    @Override
    protected int getOverlayDuration() {
        return getPreferences().getOverlayDuration(getKi2Context().getSdkContext());
    }

    @Override
    protected boolean isOverlayEnabled() {
        return getPreferences().isOverlayEnabled(getKi2Context().getSdkContext());
    }

    @Override
    protected int getOverlayPositionY() {
        return getPreferences().getOverlayPositionY(getKi2Context().getSdkContext());
    }

    @Override
    protected int getOverlayPositionX() {
        return getPreferences().getOverlayPositionX(getKi2Context().getSdkContext());
    }

    @Override
    protected String getOverlayTheme() {
        return getPreferences().getOverlayTheme(getKi2Context().getSdkContext());
    }

    @Override
    protected OverlayPreferences getOverlayPreferences() {
        return new OverlayPreferences(
                getPreferences().isOverlayEnabled(getKi2Context().getSdkContext()),
                getPreferences().getOverlayTheme(getKi2Context().getSdkContext()),
                getPreferences().getOverlayDuration(getKi2Context().getSdkContext()),
                getPreferences().getOverlayOpacity(getKi2Context().getSdkContext()),
                getPreferences().getOverlayPositionX(getKi2Context().getSdkContext()),
                getPreferences().getOverlayPositionY(getKi2Context().getSdkContext())
        );
    }
}
