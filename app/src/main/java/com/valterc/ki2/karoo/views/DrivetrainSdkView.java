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
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.views.DrivetrainView;

import java.util.function.Consumer;

public class DrivetrainSdkView extends Ki2SdkView {

    private final Consumer<ConnectionInfo> connectionInfoConsumer = connectionInfo -> {
        connectionStatus = connectionInfo.getConnectionStatus();
    };

    private final Consumer<ShiftingInfo> shiftingInfoConsumer = shiftingInfo -> {
        this.shiftingInfo = shiftingInfo;
        updateDrivetrainView();
    };

    private ConnectionStatus connectionStatus;
    private ShiftingInfo shiftingInfo;

    private TextView textView;
    private DrivetrainView drivetrainView;

    public DrivetrainSdkView(@NonNull Ki2Context context) {
        super(context);
        context.getServiceClient().registerConnectionInfoListener(connectionInfoConsumer);
        context.getServiceClient().registerShiftingInfoListener(shiftingInfoConsumer);
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
        if (connectionStatus != ConnectionStatus.ESTABLISHED || shiftingInfo == null) {
            drivetrainView.setVisibility(View.INVISIBLE);
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.INVISIBLE);
            drivetrainView.setVisibility(View.VISIBLE);
            updateDrivetrainView();
        }
    }

    private void updateDrivetrainView(){
        if (drivetrainView == null || shiftingInfo == null) {
            return;
        }

        drivetrainView.setGears(
                shiftingInfo.getFrontGearMax(),
                shiftingInfo.getFrontGear(),
                shiftingInfo.getRearGearMax(),
                shiftingInfo.getRearGear());
    }
}
