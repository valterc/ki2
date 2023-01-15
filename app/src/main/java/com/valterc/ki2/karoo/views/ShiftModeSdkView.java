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
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.views.ChangeShiftModeButtonView;

import java.util.function.BiConsumer;

public class ShiftModeSdkView extends Ki2SdkView {

    @SuppressWarnings("FieldCanBeLocal")
    private final BiConsumer<DeviceId, ConnectionInfo> connectionInfoConsumer = (deviceId, connectionInfo) -> {
        connectionStatus = connectionInfo.getConnectionStatus();
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final BiConsumer<DeviceId, ShiftingInfo> shiftingInfoConsumer = (deviceId, shiftingInfo) -> {
        this.deviceId = deviceId;
        this.shiftingInfo = shiftingInfo;
        updateView();
    };

    private DeviceId deviceId;
    private ConnectionStatus connectionStatus;
    private ShiftingInfo shiftingInfo;

    private View layoutShiftMode;
    private TextView textViewWaitingForData;
    private TextView textViewShiftMode;

    public ShiftModeSdkView(@NonNull Ki2Context context) {
        super(context);
        context.getServiceClient().registerConnectionInfoWeakListener(connectionInfoConsumer);
        context.getServiceClient().registerShiftingInfoWeakListener(shiftingInfoConsumer);
    }

    @NonNull
    @Override
    protected View createView(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup parent) {
        View inflatedView = layoutInflater.inflate(R.layout.view_karoo_shift_mode, parent, false);
        layoutShiftMode = inflatedView.findViewById(R.id.linearlayout_karoo_shift_mode);
        textViewWaitingForData = inflatedView.findViewById(R.id.textview_karoo_shift_mode_waiting_for_data);
        textViewShiftMode = inflatedView.findViewById(R.id.textview_karoo_shift_mode);
        ChangeShiftModeButtonView changeShiftModeButtonView = inflatedView.findViewById(R.id.changeshiftmodebuttonview_karoo_shift_mode_change);

        KarooTheme karooTheme = getKarooTheme(parent);

        if (karooTheme == KarooTheme.WHITE) {
            textViewWaitingForData.setTextColor(getContext().getColor(R.color.hh_black));
            textViewShiftMode.setTextColor(getContext().getColor(R.color.hh_black));
        }

        changeShiftModeButtonView.setOnActionListener(this::onAction);

        return inflatedView;
    }

    private void onAction() {
        if (deviceId == null) {
            return;
        }

        getKi2Context().getServiceClient().changeShiftMode(deviceId);
    }

    @Override
    public void onInvalid(@NonNull View view) {
    }

    @Override
    public void onUpdate(@NonNull View view, double value, @Nullable String formattedValue) {
        if (connectionStatus != ConnectionStatus.ESTABLISHED || shiftingInfo == null) {
            layoutShiftMode.setVisibility(View.INVISIBLE);
            textViewWaitingForData.setVisibility(View.VISIBLE);
        } else {
            textViewWaitingForData.setVisibility(View.INVISIBLE);
            layoutShiftMode.setVisibility(View.VISIBLE);
            updateView();
        }
    }

    private void updateView() {
        if (textViewShiftMode == null || shiftingInfo == null) {
            return;
        }

        textViewShiftMode.setText(shiftingInfo.getShiftingMode().getMode());
    }
}