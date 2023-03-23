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

public abstract class BaseOverlayView<TViewHolder extends BaseOverlayViewHolder> implements IOverlayView {

    private final Context context;

    private final TViewHolder viewHolder;

    public BaseOverlayView(Context context, TViewHolder viewHolder) {
        this.context = context;
        this.viewHolder = viewHolder;
    }

    public Context getContext() {
        return context;
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
    }

    public void hide() {
        viewHolder.getOverlayView().setVisibility(View.GONE);
    }

    @Override
    public void setAlpha(float value) {
        viewHolder.getOverlayView().setAlpha(value);
    }

    public abstract void updateView(@NonNull PreferencesView preferences,
                                    @NonNull ConnectionInfo connectionInfo,
                                    @NonNull DevicePreferencesView devicePreferences,
                                    @Nullable BatteryInfo batteryInfo,
                                    @Nullable ShiftingInfo shiftingInfo);

}
