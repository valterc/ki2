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
    };

    private ConnectionStatus connectionStatus;
    private ShiftingInfo shiftingInfo;

    public GearsSdkView(@NonNull Ki2Context context) {
        super(context.getSdkContext());
        context.getServiceClient().registerConnectionInfoListener(connectionInfoConsumer);
        context.getServiceClient().registerShiftingInfoListener(shiftingInfoConsumer);
    }

    @NonNull
    @Override
    protected View createView(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup parent) {
        return layoutInflater.inflate(R.layout.view_karoo_gears, parent, false);
    }

    @Override
    public void onInvalid(@NonNull View view) {
    }

    @Override
    public void onUpdate(@NonNull View view, double value, @Nullable String formattedValue) {
        TextView textView =  view.findViewById(R.id.textview_karoo_gears_waiting_for_data);
        GearsView gearsView =  view.findViewById(R.id.gearsview_karoo_gears);

        if (connectionStatus != ConnectionStatus.ESTABLISHED || shiftingInfo == null) {
            gearsView.setVisibility(View.INVISIBLE);
            textView.setVisibility(View.VISIBLE);
            textView.setTextColor(getContext().getColor(R.color.hh_red));
        } else {
            textView.setVisibility(View.INVISIBLE);
            gearsView.setVisibility(View.VISIBLE);

            gearsView.setRearGearMax(shiftingInfo.getRearGearMax());
            gearsView.setFrontGearMax(shiftingInfo.getFrontGearMax());
            gearsView.setRearGear(shiftingInfo.getRearGear());
            gearsView.setFrontGear(shiftingInfo.getFrontGear());
        }
    }
}