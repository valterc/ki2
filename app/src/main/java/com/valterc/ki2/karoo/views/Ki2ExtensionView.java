package com.valterc.ki2.karoo.views;

import android.content.Context;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import com.valterc.ki2.karoo.Ki2ExtensionContext;
import com.valterc.ki2.karoo.service.ServiceClient;

import io.hammerhead.karooext.models.ViewConfig;

public abstract class Ki2ExtensionView {

    private final Ki2ExtensionContext context;
    private Runnable viewUpdateListener;

    public Ki2ExtensionView(@NonNull Ki2ExtensionContext context) {
        this.context = context;
    }

    @NonNull
    protected Context getContext() {
        return context.getContext();
    }

    @NonNull
    protected Ki2ExtensionContext getKi2ExtensionContext() {
        return context;
    }

    @NonNull
    protected ServiceClient getServiceClient() {
        return context.getServiceClient();
    }

    protected KarooTheme getKarooTheme() {
        int nightModeFlags = getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES ? KarooTheme.DARK : KarooTheme.WHITE;
    }

    protected void viewUpdated() {
        if (viewUpdateListener != null) {
            viewUpdateListener.run();
        }
    }

    @NonNull
    protected abstract View createView(@NonNull LayoutInflater layoutInflater, ViewConfig viewConfig);

    public void setViewUpdateListener(Runnable listener) {
        viewUpdateListener = listener;
    }

    public View createView(ViewConfig viewConfig) {
        return createView(LayoutInflater.from(getContext()), viewConfig);
    }
}
