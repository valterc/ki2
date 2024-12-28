package com.valterc.ki2.karoo.overlay.manager;

import com.valterc.ki2.data.message.ShowOverlayMessage;
import com.valterc.ki2.karoo.Ki2ExtensionContext;
import com.valterc.ki2.karoo.overlay.OverlayPreferences;

import java.util.Set;
import java.util.function.Consumer;

public class PrimaryOverlayManager extends BaseOverlayManager {

    @SuppressWarnings("FieldCanBeLocal")
    private final Consumer<ShowOverlayMessage> showOverlayListener = (showOverlayMessage) -> showOverlay(true);

    public PrimaryOverlayManager(Ki2ExtensionContext ki2Context) {
        super(ki2Context);

        ki2Context.getServiceClient().getCustomMessageClient().registerShowOverlayWeakListener(showOverlayListener);
    }

    @Override
    protected Set<String> getOverlayTriggersSet() {
        return getPreferences().getOverlayTriggers(getExtensionContext().getContext());
    }

    @Override
    protected int getOverlayDuration() {
        return getPreferences().getOverlayDuration(getExtensionContext().getContext());
    }

    @Override
    protected boolean isOverlayEnabled() {
        return getPreferences().isOverlayEnabled(getExtensionContext().getContext());
    }

    @Override
    protected int getOverlayPositionY() {
        return getPreferences().getOverlayPositionY(getExtensionContext().getContext());
    }

    @Override
    protected int getOverlayPositionX() {
        return getPreferences().getOverlayPositionX(getExtensionContext().getContext());
    }

    @Override
    protected String getOverlayTheme() {
        return getPreferences().getOverlayTheme(getExtensionContext().getContext());
    }

    @Override
    protected OverlayPreferences getOverlayPreferences() {
        return new OverlayPreferences(
                getPreferences().isOverlayEnabled(getExtensionContext().getContext()),
                getPreferences().getOverlayTheme(getExtensionContext().getContext()),
                getPreferences().getOverlayDuration(getExtensionContext().getContext()),
                getPreferences().getOverlayOpacity(getExtensionContext().getContext()),
                getPreferences().getOverlayPositionX(getExtensionContext().getContext()),
                getPreferences().getOverlayPositionY(getExtensionContext().getContext())
        );
    }
}
