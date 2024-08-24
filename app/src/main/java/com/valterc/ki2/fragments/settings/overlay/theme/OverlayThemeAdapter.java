package com.valterc.ki2.fragments.settings.overlay.theme;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.valterc.ki2.R;
import com.valterc.ki2.data.connection.ConnectionInfo;
import com.valterc.ki2.data.connection.ConnectionStatus;
import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.data.preferences.device.DevicePreferencesView;
import com.valterc.ki2.data.shifting.BuzzerType;
import com.valterc.ki2.data.shifting.FrontTeethPattern;
import com.valterc.ki2.data.shifting.RearTeethPattern;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.data.shifting.ShiftingMode;
import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.karoo.overlay.view.IOverlayView;
import com.valterc.ki2.karoo.overlay.view.builder.OverlayViewBuilderEntry;
import com.valterc.ki2.karoo.overlay.view.builder.OverlayViewBuilderRegistry;

import java.util.function.Consumer;

import io.hammerhead.sdk.v0.SdkContext;

public class OverlayThemeAdapter extends RecyclerView.Adapter<OverlayThemeViewHolder> {

    private final Consumer<String> clickListener;
    private final LayoutInflater layoutInflater;
    private final String[] themeValues;
    private final String[] themeTitles;
    private final Ki2Context ki2Context;

    private final PreferencesView preferencesView;
    private final DevicePreferencesView devicePreferencesView;
    private final ConnectionInfo connectionInfo;
    private final BatteryInfo batteryInfo;
    private final ShiftingInfo shiftingInfo;

    public OverlayThemeAdapter(Context context, Consumer<String> clickListener) {
        this.clickListener = clickListener;
        this.layoutInflater = LayoutInflater.from(context);

        this.themeValues = context.getResources().getStringArray(R.array.preference_values_overlay_theme);
        this.themeTitles = context.getResources().getStringArray(R.array.preference_titles_overlay_theme);
        this.ki2Context = new Ki2Context(SdkContext.buildSdkContext(context));
        this.preferencesView = new PreferencesView(context);

        DeviceId deviceId = new DeviceId(67726, 1, 5);
        this.devicePreferencesView = new DevicePreferencesView(context, deviceId);
        this.connectionInfo = new ConnectionInfo(ConnectionStatus.ESTABLISHED);
        this.batteryInfo = new BatteryInfo(80);
        this.shiftingInfo = new ShiftingInfo(BuzzerType.DEFAULT, 2, 2, 5, 11, FrontTeethPattern.P50_34, RearTeethPattern.S11_P11_30, ShiftingMode.SYNCHRONIZED_SHIFT_MODE_2);
    }

    @NonNull
    @Override
    public OverlayThemeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.view_item_overlay_theme, parent, false);
        view.setOnClickListener(v -> this.clickListener.accept(themeValues[(int) view.getTag()]));
        return new OverlayThemeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OverlayThemeViewHolder holder, int position) {
        OverlayViewBuilderEntry entry = OverlayViewBuilderRegistry.getBuilder(themeValues[position]);
        View viewOverlay = layoutInflater.inflate(entry.getLayoutId(), holder.getLinearLayoutViewContainer(), false);

        holder.getRootView().setTag(position);
        holder.getTextViewName().setText(themeTitles[position]);
        holder.getLinearLayoutViewContainer().removeAllViews();
        holder.getLinearLayoutViewContainer().addView(viewOverlay);

        IOverlayView overlayView = entry.createOverlayView(ki2Context, preferencesView, viewOverlay);
        overlayView.updateView(connectionInfo, devicePreferencesView, batteryInfo, shiftingInfo);
        viewOverlay.setElevation(0);
    }

    @Override
    public int getItemCount() {
        return themeValues.length;
    }

    public int getItemIndex(String key) {
        for (int i = 0; i < themeValues.length; i++) {
            if (themeValues[i].equals(key)) {
                return i;
            }
        }
        return 0;
    }

}
