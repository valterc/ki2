package com.valterc.ki2.karoo.formatters;

import androidx.annotation.NonNull;

import com.valterc.ki2.data.connection.ConnectionInfo;
import com.valterc.ki2.data.connection.ConnectionStatus;
import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.karoo.Ki2Context;

import java.util.function.BiConsumer;

import io.hammerhead.sdk.v0.datatype.formatter.SdkFormatter;

public class BatteryStatusTextFormatter extends SdkFormatter {

    @SuppressWarnings("FieldCanBeLocal")
    private final BiConsumer<DeviceId, ConnectionInfo> connectionInfoConsumer = (deviceId, connectionInfo) -> {
        connectionStatus = connectionInfo.getConnectionStatus();
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final BiConsumer<DeviceId, BatteryInfo> batteryInfoConsumer = (deviceId, batteryInfo) -> {
        batteryValue = batteryInfo.getValue();
        batteryValueSet = true;
    };

    private ConnectionStatus connectionStatus;
    private int batteryValue;
    private boolean batteryValueSet;

    public BatteryStatusTextFormatter(Ki2Context ki2Context) {
        ki2Context.getServiceClient().registerConnectionInfoWeakListener(connectionInfoConsumer);
        ki2Context.getServiceClient().registerBatteryInfoWeakListener(batteryInfoConsumer);
    }

    @NonNull
    @Override
    public String formatValue(double value) {
        if (connectionStatus != ConnectionStatus.ESTABLISHED) {
            return NumericTextFormatterConstants.NOT_AVAILABLE;
        }

        if (!batteryValueSet) {
            return NumericTextFormatterConstants.WAITING_FOR_DATA;
        }

        if (batteryValue >= 90) {
            return "FULL";
        } else if (batteryValue >= 70) {
            return "HIGH";
        } else if (batteryValue >= 30) {
            return "MID";
        } else if (batteryValue >= 15) {
            return "LOW";
        }

        return "CRIT";
    }

}
