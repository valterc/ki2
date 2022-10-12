package com.valterc.ki2.karoo.formatters;

import androidx.annotation.NonNull;

import com.valterc.ki2.data.connection.ConnectionInfo;
import com.valterc.ki2.data.connection.ConnectionStatus;
import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.karoo.Ki2Context;

import java.text.DecimalFormat;
import java.util.function.Consumer;

import io.hammerhead.sdk.v0.datatype.formatter.SdkFormatter;

public class GearsTextFormatter extends SdkFormatter {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("00");

    private final Consumer<ConnectionInfo> connectionInfoConsumer = connectionInfo -> {
        connectionStatus = connectionInfo.getConnectionStatus();
    };

    private final Consumer<ShiftingInfo> shiftingInfoConsumer = shiftingInfo -> {
        this.shiftingInfo = shiftingInfo;
    };

    private ConnectionStatus connectionStatus;
    private ShiftingInfo shiftingInfo;

    public GearsTextFormatter(Ki2Context ki2Context) {
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

        return DECIMAL_FORMAT.format(shiftingInfo.getFrontGear()) + "-" + DECIMAL_FORMAT.format(shiftingInfo.getRearGear());
    }
}
