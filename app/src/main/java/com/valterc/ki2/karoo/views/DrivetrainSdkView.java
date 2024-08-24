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
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.views.DrivetrainView;
import com.valterc.ki2.views.battery.BatteryView;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DrivetrainSdkView extends Ki2SdkView {

    @SuppressWarnings("FieldCanBeLocal")
    private final BiConsumer<DeviceId, ConnectionInfo> connectionInfoConsumer = (deviceId, connectionInfo) -> {
        connectionStatus = connectionInfo.getConnectionStatus();
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final BiConsumer<DeviceId, ShiftingInfo> shiftingInfoConsumer = (deviceId, shiftingInfo) -> {
        this.shiftingInfo = shiftingInfo;
        updateDrivetrainView();
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final BiConsumer<DeviceId, BatteryInfo> batteryInfoConsumer = (deviceId, batteryInfo) -> {
        this.batteryInfo = batteryInfo;
        updateDrivetrainView();
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final Consumer<PreferencesView> preferencesConsumer = (preferencesView) -> {
        this.preferencesView = preferencesView;
        updateDrivetrainView();
    };

    private ConnectionStatus connectionStatus;
    private ShiftingInfo shiftingInfo;
    private BatteryInfo batteryInfo;
    private PreferencesView preferencesView;

    private KarooTheme karooTheme;
    private TextView textViewWaitingForData;
    private DrivetrainView drivetrainView;
    private BatteryView batteryView;
    private TextView textViewGears;

    public DrivetrainSdkView(@NonNull Ki2Context context) {
        super(context);
        context.getServiceClient().registerConnectionInfoWeakListener(connectionInfoConsumer);
        context.getServiceClient().registerShiftingInfoWeakListener(shiftingInfoConsumer);
        context.getServiceClient().registerBatteryInfoWeakListener(batteryInfoConsumer);
        context.getServiceClient().registerPreferencesWeakListener(preferencesConsumer);
    }

    @NonNull
    @Override
    protected View createView(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup parent) {
        View inflatedView = layoutInflater.inflate(R.layout.view_karoo_drivetrain, parent, false);
        textViewWaitingForData = inflatedView.findViewById(R.id.textview_karoo_drivetrain_waiting_for_data);
        drivetrainView = inflatedView.findViewById(R.id.drivetrainview_karoo_drivetrain);
        batteryView = inflatedView.findViewById(R.id.batteryview_karoo_drivetrain);
        textViewGears = inflatedView.findViewById(R.id.textview_karoo_drivetrain);

        karooTheme = getKarooTheme(parent);

        if (karooTheme == KarooTheme.WHITE) {
            textViewWaitingForData.setTextColor(getContext().getColor(R.color.hh_black));
            textViewGears.setTextColor(getContext().getColor(R.color.hh_black));
            drivetrainView.setTextColor(getContext().getColor(R.color.hh_black));
            drivetrainView.setChainColor(getContext().getColor(R.color.hh_black));
        } else {
            textViewWaitingForData.setTextColor(getContext().getColor(R.color.white));
            textViewGears.setTextColor(getContext().getColor(R.color.white));
            drivetrainView.setTextColor(getContext().getColor(R.color.white));
            drivetrainView.setChainColor(getContext().getColor(R.color.hh_divider_color));
        }

        return inflatedView;
    }

    @Override
    public void onInvalid(@NonNull View view) {
    }

    @Override
    public void onUpdate(@NonNull View view, double value, @Nullable String formattedValue) {
        if (connectionStatus != ConnectionStatus.ESTABLISHED || shiftingInfo == null) {
            drivetrainView.setVisibility(View.INVISIBLE);
            batteryView.setVisibility(View.INVISIBLE);
            textViewGears.setVisibility(View.INVISIBLE);
            textViewWaitingForData.setVisibility(View.VISIBLE);
        } else {
            textViewWaitingForData.setVisibility(View.INVISIBLE);
            batteryView.setVisibility(View.VISIBLE);
            textViewGears.setVisibility(View.VISIBLE);
            drivetrainView.setVisibility(View.VISIBLE);
            updateDrivetrainView();
        }
    }

    private void updateDrivetrainView(){
        if (drivetrainView == null || shiftingInfo == null || preferencesView == null) {
            return;
        }

        drivetrainView.setGears(
                shiftingInfo.getFrontGearMax(),
                shiftingInfo.getFrontGear(),
                shiftingInfo.getRearGearMax(),
                shiftingInfo.getRearGear());

        textViewGears.setText(getContext().getString(R.string.text_param_gearing,
                shiftingInfo.getFrontGear(),
                shiftingInfo.getRearGear()));
        drivetrainView.setSelectedGearColor(preferencesView.getAccentColor(getContext(), karooTheme));

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
