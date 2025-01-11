package com.valterc.ki2.karoo.views;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.valterc.ki2.R;
import com.valterc.ki2.data.connection.ConnectionInfo;
import com.valterc.ki2.data.connection.ConnectionStatus;
import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.karoo.Ki2ExtensionContext;
import com.valterc.ki2.views.GearsView;
import com.valterc.ki2.views.battery.BatteryView;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.hammerhead.karooext.models.ViewConfig;

public class GearsExtensionView extends Ki2ExtensionView {

    @SuppressWarnings("FieldCanBeLocal")
    private final BiConsumer<DeviceId, ConnectionInfo> connectionInfoConsumer = (deviceId, connectionInfo) -> {
        connectionStatus = connectionInfo.getConnectionStatus();
        updateGearsView();
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final BiConsumer<DeviceId, ShiftingInfo> shiftingInfoConsumer = (deviceId, shiftingInfo) -> {
        this.shiftingInfo = shiftingInfo;
        updateGearsView();
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final BiConsumer<DeviceId, BatteryInfo> batteryInfoConsumer = (deviceId, batteryInfo) -> {
        this.batteryInfo = batteryInfo;
        updateGearsView();
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final Consumer<PreferencesView> preferencesConsumer = (preferencesView) -> {
        this.preferencesView = preferencesView;
        updateGearsView();
    };

    private ConnectionStatus connectionStatus;
    private ShiftingInfo shiftingInfo;
    private BatteryInfo batteryInfo;
    private PreferencesView preferencesView;

    private TextView textViewWaitingForData;
    private GearsView gearsView;
    private BatteryView batteryView;
    private TextView textViewGears;

    public GearsExtensionView(@NonNull Ki2ExtensionContext context) {
        super(context);
        context.getServiceClient().registerConnectionInfoWeakListener(connectionInfoConsumer);
        context.getServiceClient().registerShiftingInfoWeakListener(shiftingInfoConsumer);
        context.getServiceClient().registerBatteryInfoWeakListener(batteryInfoConsumer);
        context.getServiceClient().registerPreferencesWeakListener(preferencesConsumer);
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    protected View createView(@NonNull LayoutInflater layoutInflater, ViewConfig viewConfig) {
        View inflatedView = layoutInflater.inflate(R.layout.view_karoo_gears, null);

        var params = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT);
        params.matchConstraintMinWidth = viewConfig.getViewSize().getFirst();
        params.matchConstraintMinHeight = viewConfig.getViewSize().getSecond();

        inflatedView.setLayoutParams(params);
        inflatedView.forceLayout();
        inflatedView.measure(
                View.MeasureSpec.makeMeasureSpec(viewConfig.getViewSize().getFirst(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(viewConfig.getViewSize().getSecond(), View.MeasureSpec.EXACTLY));
        inflatedView.layout(inflatedView.getLeft(), inflatedView.getTop(), inflatedView.getRight(), inflatedView.getBottom());

        textViewWaitingForData = inflatedView.findViewById(R.id.textview_karoo_gears_waiting_for_data);
        batteryView = inflatedView.findViewById(R.id.batteryview_karoo_gears);
        textViewGears = inflatedView.findViewById(R.id.textview_karoo_gears);
        gearsView = inflatedView.findViewById(R.id.gearsview_karoo_gears);

        if (getKarooTheme() == KarooTheme.WHITE) {
            textViewWaitingForData.setTextColor(getContext().getColor(R.color.hh_black));
            textViewGears.setTextColor(getContext().getColor(R.color.hh_black));
            gearsView.setUnselectedGearBorderColor(getContext().getColor(R.color.hh_gears_border_light));
            gearsView.setSelectedGearColor(getContext().getColor(R.color.hh_gears_active_light));
        } else {
            textViewWaitingForData.setTextColor(getContext().getColor(R.color.white));
            textViewGears.setTextColor(getContext().getColor(R.color.white));
            gearsView.setUnselectedGearBorderColor(getContext().getColor(R.color.hh_gears_border_dark));
            gearsView.setSelectedGearColor(getContext().getColor(R.color.hh_gears_active_dark));
        }

        return inflatedView;
    }

    private void updateGearsView() {
        if (gearsView == null) {
            return;
        }

        if (shiftingInfo == null || connectionStatus != ConnectionStatus.ESTABLISHED || preferencesView == null) {
            gearsView.setVisibility(View.INVISIBLE);
            batteryView.setVisibility(View.INVISIBLE);
            textViewGears.setVisibility(View.INVISIBLE);
            textViewWaitingForData.setVisibility(View.VISIBLE);

            viewUpdated();
            return;
        } else {
            textViewWaitingForData.setVisibility(View.INVISIBLE);
            gearsView.setVisibility(View.VISIBLE);
            batteryView.setVisibility(View.VISIBLE);
            textViewGears.setVisibility(View.VISIBLE);
        }

        gearsView.setGears(
                shiftingInfo.getFrontGearMax(),
                shiftingInfo.getFrontGear(),
                shiftingInfo.getRearGearMax(),
                shiftingInfo.getRearGear());

        textViewGears.setText(getContext().getString(R.string.text_param_gearing,
                shiftingInfo.getFrontGear(),
                shiftingInfo.getRearGear()));
        gearsView.setSelectedGearColor(preferencesView.getAccentColor(getContext(), getKarooTheme()));

        if (batteryInfo == null) {
            batteryView.setForegroundColor(getContext().getColor(R.color.battery_background_dark));
            batteryView.setBorderColor(getContext().getColor(R.color.battery_border_dark));
        } else {
            batteryView.setValue((float) batteryInfo.getValue() / 100);

            var criticalBatteryLevel = preferencesView.getBatteryLevelCritical(getContext());
            var lowBatteryLevel = preferencesView.getBatteryLevelLow(getContext());

            if (criticalBatteryLevel != null && batteryInfo.getValue() <= criticalBatteryLevel) {
                batteryView.setForegroundColor(getContext().getColor(R.color.hh_red));
                batteryView.setBorderColor(getContext().getColor(R.color.hh_red));
            } else if (lowBatteryLevel != null && batteryInfo.getValue() <= lowBatteryLevel) {
                batteryView.setForegroundColor(getContext().getColor(R.color.hh_yellow_darker));
                batteryView.setBorderColor(getContext().getColor(R.color.hh_yellow_darker));
            } else {
                batteryView.setForegroundColor(getContext().getColor(R.color.hh_green));
                batteryView.setBorderColor(getContext().getColor(R.color.hh_green));
            }
        }

        viewUpdated();
    }
}