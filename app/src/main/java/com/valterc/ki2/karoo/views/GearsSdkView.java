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
import com.valterc.ki2.views.GearsView;

import java.util.function.Consumer;

import io.hammerhead.sdk.v0.datatype.view.SdkView;

public class GearsSdkView extends SdkView {

    private final Consumer<ConnectionInfo> connectionInfoConsumer = connectionInfo -> {
        connectionStatus = connectionInfo.getConnectionStatus();
    };

    private final Consumer<ShiftingInfo> shiftingInfoConsumer = shiftingInfo -> {
        this.shiftingInfo = shiftingInfo;
        updateGearsView();
    };

    private ConnectionStatus connectionStatus;
    private ShiftingInfo shiftingInfo;

    private TextView textView;
    private GearsView gearsView;

    public GearsSdkView(@NonNull Ki2Context context) {
        super(context.getSdkContext());
        context.getServiceClient().registerConnectionInfoListener(connectionInfoConsumer);
        context.getServiceClient().registerShiftingInfoListener(shiftingInfoConsumer);
    }

    @NonNull
    @Override
    protected View createView(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup parent) {
        View inflatedView = layoutInflater.inflate(R.layout.view_karoo_gears, parent, false);
        textView = inflatedView.findViewById(R.id.textview_karoo_gears_waiting_for_data);
        gearsView = inflatedView.findViewById(R.id.gearsview_karoo_gears);
        return inflatedView;
    }

    @Override
    public void onInvalid(@NonNull View view) {
    }

    @Override
    public void onUpdate(@NonNull View view, double value, @Nullable String formattedValue) {
        if (connectionStatus != ConnectionStatus.ESTABLISHED || shiftingInfo == null) {
            gearsView.setVisibility(View.INVISIBLE);
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.INVISIBLE);
            gearsView.setVisibility(View.VISIBLE);
            updateGearsView();
        }
    }

    private void updateGearsView() {
        if (gearsView == null || shiftingInfo == null) {
            return;
        }

        gearsView.setGears(
                shiftingInfo.getFrontGearMax(),
                shiftingInfo.getFrontGear(),
                shiftingInfo.getRearGearMax(),
                shiftingInfo.getRearGear());
    }
}