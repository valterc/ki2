package com.valterc.ki2.karoo.views;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.valterc.ki2.R;
import com.valterc.ki2.data.connection.ConnectionInfo;
import com.valterc.ki2.data.connection.ConnectionStatus;
import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.data.preferences.device.DevicePreferencesView;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.karoo.shifting.ShiftingGearingHelper;
import com.valterc.ki2.views.GearsView;
import com.valterc.ki2.views.battery.BatteryView;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GearsSizeSdkView extends Ki2SdkView {

    private ConnectionStatus connectionStatus;
    private ShiftingGearingHelper shiftingGearingHelper;
    private BatteryInfo batteryInfo;
    private PreferencesView preferencesView;


    @SuppressWarnings("FieldCanBeLocal")
    private final BiConsumer<DeviceId, ConnectionInfo> connectionInfoConsumer = (deviceId, connectionInfo) ->
            connectionStatus = connectionInfo.getConnectionStatus();

    @SuppressWarnings("FieldCanBeLocal")
    private final BiConsumer<DeviceId, ShiftingInfo> shiftingInfoConsumer = (deviceId, shiftingInfo) -> {
        shiftingGearingHelper.setShiftingInfo(shiftingInfo);
        updateGearsView();
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final BiConsumer<DeviceId, DevicePreferencesView> devicePreferencesConsumer = (deviceId, devicePreferences) -> {
        shiftingGearingHelper.setDevicePreferences(devicePreferences);
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

    private KarooTheme karooTheme;
    private TextView textViewWaitingForData;
    private GearsView gearsView;
    private BatteryView batteryView;
    private TextView textViewGears;

    public GearsSizeSdkView(@NonNull Ki2Context context) {
        super(context);
        shiftingGearingHelper = new ShiftingGearingHelper(context.getSdkContext());
        context.getServiceClient().registerConnectionInfoWeakListener(connectionInfoConsumer);
        context.getServiceClient().registerShiftingInfoWeakListener(shiftingInfoConsumer);
        context.getServiceClient().registerDevicePreferencesWeakListener(devicePreferencesConsumer);
        context.getServiceClient().registerBatteryInfoWeakListener(batteryInfoConsumer);
        context.getServiceClient().registerPreferencesWeakListener(preferencesConsumer);
    }

    @NonNull
    @Override
    protected View createView(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup parent) {
        View inflatedView = layoutInflater.inflate(R.layout.view_karoo_gears, parent, false);
        textViewWaitingForData = inflatedView.findViewById(R.id.textview_karoo_gears_waiting_for_data);
        gearsView = inflatedView.findViewById(R.id.gearsview_karoo_gears);
        batteryView = inflatedView.findViewById(R.id.batteryview_karoo_gears);
        textViewGears = inflatedView.findViewById(R.id.textview_karoo_gears);

        karooTheme = getKarooTheme(parent);

        if (karooTheme == KarooTheme.WHITE) {
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

    @Override
    public void onInvalid(@NonNull View view) {
    }

    @Override
    public void onUpdate(@NonNull View view, double value, @Nullable String formattedValue) {
        if (connectionStatus != ConnectionStatus.ESTABLISHED || shiftingGearingHelper.hasInvalidGearingInfo()) {
            gearsView.setVisibility(View.INVISIBLE);
            batteryView.setVisibility(View.INVISIBLE);
            textViewGears.setVisibility(View.INVISIBLE);
            textViewWaitingForData.setVisibility(View.VISIBLE);
        } else {
            textViewWaitingForData.setVisibility(View.INVISIBLE);
            gearsView.setVisibility(View.VISIBLE);
            batteryView.setVisibility(View.VISIBLE);
            textViewGears.setVisibility(View.VISIBLE);
            updateGearsView();
        }
    }

    private void updateGearsView() {
        if (gearsView == null || shiftingGearingHelper.hasInvalidGearingInfo() || preferencesView == null) {
            return;
        }

        gearsView.setGears(
                shiftingGearingHelper.getFrontGearMax(),
                shiftingGearingHelper.getFrontGear(),
                shiftingGearingHelper.getRearGearMax(),
                shiftingGearingHelper.getRearGear());

        textViewGears.setText(getContext().getString(R.string.text_param_gearing,
                shiftingGearingHelper.getFrontGearTeethCount(),
                shiftingGearingHelper.getRearGearTeethCount()));
        gearsView.setSelectedGearColor(preferencesView.getAccentColor(getContext(), karooTheme));

        if (batteryInfo == null) {
            batteryView.setForegroundColor(getContext().getColor(R.color.battery_background_dark));
            batteryView.setBorderColor(getContext().getColor(R.color.battery_border_dark));
        } else {
            batteryView.setValue((float) batteryInfo.getValue() / 100);

            if (preferencesView != null &&
                    preferencesView.getBatteryLevelCritical(getContext()) != null &&
                    batteryInfo.getValue() <= preferencesView.getBatteryLevelCritical(getContext())) {
                batteryView.setForegroundColor(getContext().getColor(R.color.hh_red));
                batteryView.setBorderColor(getContext().getColor(R.color.hh_red));
            } else if (preferencesView != null &&
                    preferencesView.getBatteryLevelLow(getContext()) != null &&
                    batteryInfo.getValue() <= preferencesView.getBatteryLevelLow(getContext())) {
                batteryView.setForegroundColor(getContext().getColor(R.color.hh_yellow_darker));
                batteryView.setBorderColor(getContext().getColor(R.color.hh_yellow_darker));
            } else {
                batteryView.setForegroundColor(getContext().getColor(R.color.hh_green));
                batteryView.setBorderColor(getContext().getColor(R.color.hh_green));
            }
        }
    }
}