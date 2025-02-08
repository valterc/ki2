package com.valterc.ki2.karoo.overlay.view;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.valterc.ki2.data.connection.ConnectionInfo;
import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.data.preferences.device.DevicePreferencesView;
import com.valterc.ki2.data.shifting.ShiftingInfo;

import java.util.function.Consumer;

public abstract class BaseOverlayView<TViewHolder extends BaseOverlayViewHolder> implements IOverlayView {

    private final Context context;
    private final PreferencesView preferences;

    private final TViewHolder viewHolder;

    private Consumer<Boolean> visibilityListener;

    public BaseOverlayView(Context context, PreferencesView preferences, TViewHolder viewHolder) {
        this.context = context;
        this.preferences = preferences;
        this.viewHolder = viewHolder;
    }

    public Context getContext() {
        return context;
    }

    public PreferencesView getPreferences() {
        return preferences;
    }

    protected TViewHolder getViewHolder() {
        return viewHolder;
    }

    @Override
    public void remove() {
        viewHolder.removeFromHierarchy();
    }

    public void show() {
        viewHolder.getOverlayView().setVisibility(View.VISIBLE);

        if (visibilityListener != null) {
            visibilityListener.accept(true);
        }
    }

    public void hide() {
        viewHolder.getOverlayView().setVisibility(View.GONE);

        if (visibilityListener != null) {
            visibilityListener.accept(false);
        }
    }

    @Override
    public boolean isVisible() {
        return viewHolder.getOverlayView().getVisibility() == View.VISIBLE;
    }

    @Override
    public void setVisibilityListener(Consumer<Boolean> visibilityListener) {
        this.visibilityListener = visibilityListener;
    }

    @Override
    public void setAlpha(float value) {
        viewHolder.getOverlayView().setAlpha(value);
    }

    public abstract void updateView(@NonNull ConnectionInfo connectionInfo,
                                    @NonNull DevicePreferencesView devicePreferences,
                                    @Nullable BatteryInfo batteryInfo,
                                    @Nullable ShiftingInfo shiftingInfo);

}
