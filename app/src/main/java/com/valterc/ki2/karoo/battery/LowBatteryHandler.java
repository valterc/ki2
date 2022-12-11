package com.valterc.ki2.karoo;

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
import com.valterc.ki2.karoo.hooks.KarooActivityServiceNotificationControllerHook;
import com.valterc.ki2.karoo.hooks.KarooAudioAlertHook;
import com.valterc.ki2.karoo.notification.LowBatteryCategory;
import com.valterc.ki2.karoo.notification.LowBatteryNotification;

@SuppressLint("LogNotTimber")
public class LowBatteryHandler {

    private static final long TIME_NOTIFY_AFTER_PAUSE_MS = 2 * 60 * 1000;

    private final Ki2Context context;
    private final Integer batteryLevelLow;
    private final Integer batteryLevelCritical;
    private DeviceId notifiedDeviceId;
    private BatteryInfo notifiedBatteryInfo;
    private LowBatteryCategory notifiedCategory;
    private boolean notified;
    private long pauseTimestamp;

    public LowBatteryHandler(Ki2Context context) {
        this.context = context;
        context.getServiceClient().registerBatteryInfoWeakListener(this::onBattery);

        PreferencesView preferences = context.getServiceClient().getPreferences();
        batteryLevelLow = preferences.getBatteryLevelLow(context.getSdkContext());
        batteryLevelCritical = preferences.getBatteryLevelCritical(context.getSdkContext());
    }

    public void onPause() {
        pauseTimestamp = System.currentTimeMillis();
    }

    public void onResume() {
        if (notified && System.currentTimeMillis() - pauseTimestamp > TIME_NOTIFY_AFTER_PAUSE_MS) {
            notify(notifiedDeviceId, notifiedBatteryInfo, notifiedCategory);
        }
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
        LowBatteryCategory category = getCategory(batteryInfo);

        if (category != null && !category.equals(notifiedCategory)) {
            this.notifiedDeviceId = deviceId;
            this.notifiedBatteryInfo = batteryInfo;
            this.notifiedCategory = category;

            notify(deviceId, batteryInfo, category);
            notified = true;
        }
    }

    private void notify(DeviceId deviceId, BatteryInfo batteryInfo, LowBatteryCategory category) {
        if (deviceId == null || batteryInfo == null || category == null) {
            return;
        }

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            Log.d("KI2", "Low battery notification");

            boolean karooNotificationResult = KarooActivityServiceNotificationControllerHook.showSensorLowBatteryNotification(context.getSdkContext(), deviceId.getName());
            KarooAudioAlertHook.triggerLowBatteryAudioAlert(context.getSdkContext());
            LowBatteryNotification.showLowBatteryNotification(context.getSdkContext(), deviceId.getName(), category, batteryInfo.getValue());

            if (!karooNotificationResult) {
                Toast toast = Toast.makeText(context.getSdkContext(), context.getSdkContext().getString(R.string.text_param_di2_low_battery, deviceId.getName(), batteryInfo.getValue()), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
            }
        }, 500);
    }

}
