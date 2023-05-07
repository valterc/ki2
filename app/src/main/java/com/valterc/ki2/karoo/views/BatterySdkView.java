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
import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.views.fill.FillView;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class BatterySdkView extends Ki2SdkView {

    @SuppressWarnings("FieldCanBeLocal")
    private final BiConsumer<DeviceId, ConnectionInfo> connectionInfoConsumer = (deviceId, connectionInfo) -> {
        connectionStatus = connectionInfo.getConnectionStatus();
        updateValue();
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final BiConsumer<DeviceId, BatteryInfo> batteryInfoConsumer = (deviceId, batteryInfo) -> {
        this.batteryInfo = batteryInfo;
        updateValue();
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final Consumer<PreferencesView> preferencesConsumer = (preferencesView) -> {
        batteryLevelLow = preferencesView.getBatteryLevelLow(getContext());
        batteryLevelCritical = preferencesView.getBatteryLevelLow(getContext());
        updateValue();
    };

    private Integer batteryLevelLow;
    private Integer batteryLevelCritical;
    private ConnectionStatus connectionStatus;
    private BatteryInfo batteryInfo;

    private TextView textView;
    private FillView fillView;

    public BatterySdkView(@NonNull Ki2Context context) {
        super(context);
        context.getServiceClient().registerPreferencesWeakListener(preferencesConsumer);
        context.getServiceClient().registerConnectionInfoWeakListener(connectionInfoConsumer);
        context.getServiceClient().registerBatteryInfoWeakListener(batteryInfoConsumer);
    }

    @NonNull
    @Override
    protected View createView(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup parent) {
        View inflatedView = layoutInflater.inflate(R.layout.view_karoo_battery, parent, false);
        textView = inflatedView.findViewById(R.id.textview_karoo_battery);
        fillView = inflatedView.findViewById(R.id.fillview_karoo_battery);

        KarooTheme karooTheme = getKarooTheme(parent);

        if (karooTheme == KarooTheme.WHITE) {
            textView.setTextColor(getContext().getColor(R.color.hh_black));
        }

        return inflatedView;
    }

    @Override
    public void onInvalid(@NonNull View view) {
    }

    @Override
    public void onUpdate(@NonNull View view, double value, @Nullable String formattedValue) {
        updateValue();
    }

    private void updateValue() {
        if (fillView == null) {
            return;
        }

        if (connectionStatus == null || connectionStatus != ConnectionStatus.ESTABLISHED || batteryInfo == null) {
            fillView.setForegroundColor(getContext().getColor(R.color.hh_grey));
            fillView.setValue(0);
            textView.setText(R.string.text_na);
        } else {
            if (batteryLevelCritical != null && batteryInfo.getValue() <= batteryLevelCritical) {
                fillView.setForegroundColor(getContext().getColor(R.color.hh_red_600));
            } else if (batteryLevelLow != null && batteryInfo.getValue() <= batteryLevelLow) {
                fillView.setForegroundColor(getContext().getColor(R.color.hh_yellow));
            } else {
                fillView.setForegroundColor(getContext().getColor(R.color.hh_success_green_600));
            }

            textView.setText(getContext().getString(R.string.text_param_percentage, batteryInfo.getValue()));
            fillView.setValue(batteryInfo.getValue() * 0.01f);
        }
    }

}
