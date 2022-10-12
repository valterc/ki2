package com.valterc.ki2.karoo.formatters;

import androidx.annotation.NonNull;

import com.valterc.ki2.data.connection.ConnectionInfo;
import com.valterc.ki2.data.connection.ConnectionStatus;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.karoo.Ki2Context;

import java.util.function.Consumer;

import io.hammerhead.sdk.v0.datatype.formatter.SdkFormatter;

public class ShiftModeTextFormatter extends SdkFormatter {

    private final Consumer<ConnectionInfo> connectionInfoConsumer = connectionInfo -> {
        connectionStatus = connectionInfo.getConnectionStatus();
    };

    private final Consumer<ShiftingInfo> shiftingInfoConsumer = shiftingInfo -> {
        this.shiftingInfo = shiftingInfo;
    };

    private ConnectionStatus connectionStatus;
    private ShiftingInfo shiftingInfo;

    public ShiftModeTextFormatter(Ki2Context ki2Context) {
        ki2Context.getServiceClient().registerConnectionInfoListener(connectionInfoConsumer);
        ki2Context.getServiceClient().registerShiftingInfoListener(shiftingInfoConsumer);
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

        return shiftingInfo.getShiftingMode().getMode();
    }
}