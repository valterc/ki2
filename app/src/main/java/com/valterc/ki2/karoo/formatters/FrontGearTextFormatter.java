package com.valterc.ki2.karoo.formatters;

import androidx.annotation.NonNull;

import com.valterc.ki2.data.connection.ConnectionInfo;
import com.valterc.ki2.data.connection.ConnectionStatus;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.karoo.Ki2Context;

import java.text.DecimalFormat;
import java.util.function.BiConsumer;

import io.hammerhead.sdk.v0.datatype.formatter.SdkFormatter;

public class FrontGearTextFormatter extends SdkFormatter {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("00");

    @SuppressWarnings("FieldCanBeLocal")
    private final BiConsumer<DeviceId, ConnectionInfo> connectionInfoConsumer = (deviceId, connectionInfo) ->
            connectionStatus = connectionInfo.getConnectionStatus();

    @SuppressWarnings("FieldCanBeLocal")
    private final BiConsumer<DeviceId, ShiftingInfo> shiftingInfoConsumer = (deviceId, shiftingInfo) ->
            this.shiftingInfo = shiftingInfo;

    private ConnectionStatus connectionStatus;
    private ShiftingInfo shiftingInfo;

    public FrontGearTextFormatter(Ki2Context ki2Context) {
        ki2Context.getServiceClient().registerConnectionInfoWeakListener(connectionInfoConsumer);
        ki2Context.getServiceClient().registerShiftingInfoWeakListener(shiftingInfoConsumer);
    }

    @NonNull
    @Override
    public String formatValue(double value) {
        if (connectionStatus != ConnectionStatus.ESTABLISHED) {
            return NumericTextFormatterConstants.NOT_AVAILABLE;
        }

        if (shiftingInfo == null) {
            return NumericTextFormatterConstants.WAITING_FOR_DATA;
        }

        return DECIMAL_FORMAT.format(shiftingInfo.getFrontGear());
    }
}