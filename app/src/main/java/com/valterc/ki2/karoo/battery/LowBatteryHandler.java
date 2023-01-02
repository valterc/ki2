package com.valterc.ki2.karoo.battery;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.valterc.ki2.R;
import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.karoo.handlers.IRideHandler;
import com.valterc.ki2.karoo.hooks.ActivityServiceNotificationControllerHook;
import com.valterc.ki2.karoo.hooks.AudioAlertHook;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SuppressWarnings("FieldCanBeLocal")
@SuppressLint("LogNotTimber")
public class LowBatteryHandler implements IRideHandler {

    private static final long TIME_MS_NOTIFY_AFTER_PAUSE = 5 * 60 * 1000;

    private final Ki2Context context;
    private final Handler handler;
    private final Map<DeviceId, LowBatteryRecord> deviceNotificationMap;
    private Integer batteryLevelLow;
    private Integer batteryLevelCritical;
    private boolean riding;
    private long pauseTimestamp;

    private final Consumer<PreferencesView> onPreferences = this::onPreferences;
    private final BiConsumer<DeviceId, BatteryInfo> onBattery =  this::onBattery;

    public LowBatteryHandler(Ki2Context context) {
        this.context = context;
        this.deviceNotificationMap = new HashMap<>();
        this.handler = new Handler(Looper.getMainLooper());

        context.getServiceClient().registerPreferencesWeakListener(onPreferences);
        context.getServiceClient().registerBatteryInfoWeakListener(onBattery);
    }

    private void onPreferences(PreferencesView preferencesView) {
        batteryLevelLow = preferencesView.getBatteryLevelLow(context.getSdkContext());
        batteryLevelCritical = preferencesView.getBatteryLevelCritical(context.getSdkContext());
        deviceNotificationMap.forEach((deviceId, record) -> performBatteryCheck(deviceId, record.getBatteryInfo()));
    }

    @Override
    public void onRideStart() {
        riding = true;
        deviceNotificationMap.values().stream()
                .filter(LowBatteryRecord::shouldNotifyInRide)
                .forEach(this::notify);
    }

    @Override
    public void onRidePause() {
        pauseTimestamp = System.currentTimeMillis();
    }

    @Override
    public void onRideResume() {
        if (System.currentTimeMillis() - pauseTimestamp > TIME_MS_NOTIFY_AFTER_PAUSE) {
            deviceNotificationMap.forEach((deviceId, record) -> notify(record));
        }
    }

    @Override
    public void onRideEnd() {
        riding = false;
    }

    private LowBatteryCategory getCategory(BatteryInfo batteryInfo) {
        if (batteryLevelCritical != null && batteryInfo.getValue() <= batteryLevelCritical) {
            return LowBatteryCategory.CRITICAL;
        } else if (batteryLevelLow != null && batteryInfo.getValue() <= batteryLevelLow) {
            return LowBatteryCategory.LOW;
        }

        return null;
    }

    private void onBattery(DeviceId deviceId, BatteryInfo batteryInfo) {
        performBatteryCheck(deviceId, batteryInfo);
    }

    private void performBatteryCheck(DeviceId deviceId, BatteryInfo batteryInfo) {
        LowBatteryCategory category = getCategory(batteryInfo);
        if (category == null) {
            return;
        }

        LowBatteryRecord record = deviceNotificationMap.get(deviceId);

        if (record == null) {
            record = new LowBatteryRecord(deviceId, batteryInfo, category);
            deviceNotificationMap.put(deviceId, record);
        } else if (category == record.getCategory()) {
            return;
        } else {
            record.setCategory(category);
        }

        notify(record);
    }

    private void notify(LowBatteryRecord record) {
        if (record == null || !record.shouldNotify()) {
            return;
        }

        DeviceId deviceId = record.getDeviceId();
        BatteryInfo batteryInfo = record.getBatteryInfo();
        LowBatteryCategory category = record.getCategory();

        handler.postDelayed(() -> {
            Log.d("KI2", "Low battery notification");
            record.markNotified();

            if (riding) {
                record.markNotifiedInRide();
                boolean karooNotificationResult = ActivityServiceNotificationControllerHook.showSensorLowBatteryNotification(context.getSdkContext(), deviceId.getName());
                if (!karooNotificationResult) {
                    Toast toast = Toast.makeText(context.getSdkContext(), context.getSdkContext().getString(R.string.text_param_di2_low_battery, deviceId.getName(), batteryInfo.getValue()), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
                    toast.show();
                }
                AudioAlertHook.triggerLowBatteryAudioAlert(context.getSdkContext());
            }

            LowBatteryNotification.showLowBatteryNotification(context.getSdkContext(), deviceId.getName(), category, batteryInfo.getValue());
        }, 500);
    }

}
