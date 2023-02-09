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
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.preferences.device.DevicePreferencesView;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.karoo.formatters.NumericTextFormatterConstants;
import com.valterc.ki2.karoo.shifting.ShiftingGearingHelper;
import com.valterc.ki2.views.DrivetrainView;

import java.util.function.BiConsumer;

public class DrivetrainSizeSdkView extends Ki2SdkView {

    private ConnectionStatus connectionStatus;
    private ShiftingGearingHelper shiftingGearingHelper;

    @SuppressWarnings("FieldCanBeLocal")
    private final BiConsumer<DeviceId, ConnectionInfo> connectionInfoConsumer = (deviceId, connectionInfo) ->
            connectionStatus = connectionInfo.getConnectionStatus();

    @SuppressWarnings("FieldCanBeLocal")
    private final BiConsumer<DeviceId, ShiftingInfo> shiftingInfoConsumer = (deviceId, shiftingInfo) -> {
        shiftingGearingHelper.setShiftingInfo(shiftingInfo);
        updateDrivetrainView();
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final BiConsumer<DeviceId, DevicePreferencesView> devicePreferencesConsumer = (deviceId, devicePreferences) -> {
        shiftingGearingHelper.setDevicePreferences(devicePreferences);
        updateDrivetrainView();
    };

    private TextView textView;
    private DrivetrainView drivetrainView;

    public DrivetrainSizeSdkView(@NonNull Ki2Context context) {
        super(context);
        shiftingGearingHelper = new ShiftingGearingHelper(context.getSdkContext());
        context.getServiceClient().registerConnectionInfoWeakListener(connectionInfoConsumer);
        context.getServiceClient().registerShiftingInfoWeakListener(shiftingInfoConsumer);
        context.getServiceClient().registerDevicePreferencesWeakListener(devicePreferencesConsumer);
    }

    @NonNull
    @Override
    protected View createView(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup parent) {
        View inflatedView = layoutInflater.inflate(R.layout.view_karoo_drivetrain, parent, false);
        textView = inflatedView.findViewById(R.id.textview_karoo_drivetrain_waiting_for_data);
        drivetrainView = inflatedView.findViewById(R.id.drivetrainview_karoo_drivetrain);

        KarooTheme karooTheme = getKarooTheme(parent);

        if (karooTheme == KarooTheme.WHITE) {
            textView.setTextColor(getContext().getColor(R.color.hh_black));
            drivetrainView.setTextColor(getContext().getColor(R.color.hh_black));
            drivetrainView.setChainColor(getContext().getColor(R.color.hh_black));
        }

        return inflatedView;
    }

    @Override
    public void onInvalid(@NonNull View view) {
    }

    @Override
    public void onUpdate(@NonNull View view, double value, @Nullable String formattedValue) {
        if (connectionStatus != ConnectionStatus.ESTABLISHED || !shiftingGearingHelper.hasGearingInfo()) {
            drivetrainView.setVisibility(View.INVISIBLE);
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.INVISIBLE);
            drivetrainView.setVisibility(View.VISIBLE);
            updateDrivetrainView();
        }
    }

    private void updateDrivetrainView(){
        if (drivetrainView == null || !shiftingGearingHelper.hasGearingInfo()) {
            return;
        }

        int frontTeethCount = shiftingGearingHelper.getFrontGearTeethCount();
        int rearTeethCount = shiftingGearingHelper.getRearGearTeethCount();

        drivetrainView.setGears(
                shiftingGearingHelper.getFrontGearMax(),
                shiftingGearingHelper.getFrontGear(),
                frontTeethCount == 0 ? NumericTextFormatterConstants.NOT_AVAILABLE : String.valueOf(frontTeethCount),
                shiftingGearingHelper.getRearGearMax(),
                shiftingGearingHelper.getRearGear(),
                rearTeethCount == 0 ? NumericTextFormatterConstants.NOT_AVAILABLE : String.valueOf(rearTeethCount));
    }
}
