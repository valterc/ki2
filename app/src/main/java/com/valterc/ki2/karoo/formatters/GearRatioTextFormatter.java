package com.valterc.ki2.karoo.formatters;

import androidx.annotation.NonNull;

import com.valterc.ki2.data.connection.ConnectionInfo;
import com.valterc.ki2.data.connection.ConnectionStatus;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.preferences.device.DevicePreferencesView;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.karoo.extension.shifting.ShiftingGearingHelper;

import java.text.DecimalFormat;
import java.util.function.BiConsumer;

import io.hammerhead.sdk.v0.datatype.formatter.SdkFormatter;

public class GearRatioTextFormatter extends SdkFormatter {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.00");

    private ShiftingGearingHelper shiftingGearingHelper;
    private ConnectionStatus connectionStatus;

    @SuppressWarnings("FieldCanBeLocal")
    private final BiConsumer<DeviceId, ConnectionInfo> connectionInfoConsumer = (deviceId, connectionInfo) ->
            connectionStatus = connectionInfo.getConnectionStatus();

    @SuppressWarnings("FieldCanBeLocal")
    private final BiConsumer<DeviceId, ShiftingInfo> shiftingInfoConsumer = (deviceId, shiftingInfo) ->
            shiftingGearingHelper.setShiftingInfo(shiftingInfo);

    @SuppressWarnings("FieldCanBeLocal")
    private final BiConsumer<DeviceId, DevicePreferencesView> devicePreferencesConsumer = (deviceId, devicePreferences) ->
            shiftingGearingHelper.setDevicePreferences(devicePreferences);

    public GearRatioTextFormatter(Ki2Context ki2Context) {
        shiftingGearingHelper = new ShiftingGearingHelper(ki2Context.getSdkContext());
        ki2Context.getServiceClient().registerConnectionInfoWeakListener(connectionInfoConsumer);
        ki2Context.getServiceClient().registerShiftingInfoWeakListener(shiftingInfoConsumer);
        ki2Context.getServiceClient().registerDevicePreferencesWeakListener(devicePreferencesConsumer);
    }

    @NonNull
    @Override
    public String formatValue(double value) {
        if (connectionStatus != ConnectionStatus.ESTABLISHED) {
            return NumericTextFormatterConstants.NOT_AVAILABLE;
        }

        if (shiftingGearingHelper.hasInvalidGearingInfo()) {
            return NumericTextFormatterConstants.WAITING_FOR_DATA;
        }

        if (shiftingGearingHelper.getFrontGearTeethCount() == 0 ||
                shiftingGearingHelper.getRearGearTeethCount() == 0) {
            return NumericTextFormatterConstants.UNKNOWN;
        }

        return DECIMAL_FORMAT.format(shiftingGearingHelper.getGearRatio());
    }
}