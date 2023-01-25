package com.valterc.ki2.karoo.formatters;

import android.content.Context;

import androidx.annotation.NonNull;

import com.valterc.ki2.data.connection.ConnectionInfo;
import com.valterc.ki2.data.connection.ConnectionStatus;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.preferences.device.DevicePreferencesView;
import com.valterc.ki2.karoo.Ki2Context;

import java.util.function.BiConsumer;

import io.hammerhead.sdk.v0.datatype.formatter.SdkFormatter;

public class DeviceNameTextFormatter extends SdkFormatter {

    private Context context;

    @SuppressWarnings("FieldCanBeLocal")
    private final BiConsumer<DeviceId, ConnectionInfo> connectionInfoConsumer = (deviceId, connectionInfo) -> {
        connectionStatus = connectionInfo.getConnectionStatus();
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final BiConsumer<DeviceId, DevicePreferencesView> devicePreferencesConsumer = (deviceId, preferences) -> {
        deviceName = preferences.getName(context);
    };

    private ConnectionStatus connectionStatus;
    private String deviceName;

    public DeviceNameTextFormatter(Ki2Context ki2Context) {
        this.context = ki2Context.getSdkContext();
        ki2Context.getServiceClient().registerConnectionInfoWeakListener(connectionInfoConsumer);
        ki2Context.getServiceClient().registerDevicePreferencesWeakListener(devicePreferencesConsumer);
    }

    @NonNull
    @Override
    public String formatValue(double value) {
        if (connectionStatus != ConnectionStatus.ESTABLISHED) {
            return NumericTextFormatterConstants.NOT_AVAILABLE;
        }

        if (deviceName == null) {
            return NumericTextFormatterConstants.WAITING_FOR_DATA;
        }

        return deviceName;
    }

}
