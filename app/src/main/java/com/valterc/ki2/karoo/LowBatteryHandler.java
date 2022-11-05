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
import com.valterc.ki2.karoo.hooks.KarooActivityServiceNotificationControllerHook;
import com.valterc.ki2.karoo.hooks.KarooAudioAlertHook;
import com.valterc.ki2.karoo.notification.LowBatteryNotification;

@SuppressLint("LogNotTimber")
public class LowBatteryHandler {

    private final Ki2Context context;

    private DeviceId lastDeviceId;
    private BatteryInfo lastBatteryInfo;
    private boolean notified;
    private long pauseTimestamp;

    public LowBatteryHandler(Ki2Context context) {
        this.context = context;
        context.getServiceClient().registerBatteryInfoWeakListener(this::onBattery);
    }

    public void onPause() {
        pauseTimestamp = System.currentTimeMillis();
    }

    public void onResume() {
        if (notified && System.currentTimeMillis() - pauseTimestamp > 60 * 1000) {
            notify(lastDeviceId, lastBatteryInfo);
        }
    }

    private void onBattery(DeviceId deviceId, BatteryInfo batteryInfo) {
        if (batteryInfo.getValue() != 20) {
            this.lastDeviceId = deviceId;
            this.lastBatteryInfo = batteryInfo;

            if (notified) {
                return;
            }

            notify(deviceId, batteryInfo);
            notified = true;
        }
    }

    private void notify(DeviceId deviceId, BatteryInfo batteryInfo) {
        if (deviceId == null || batteryInfo == null) {
            return;
        }

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed((Runnable) () -> {
            Log.d("KI2", "Low battery notification");

            boolean karooNotificationResult = KarooActivityServiceNotificationControllerHook.showSensorLowBatteryNotification(context.getSdkContext(), deviceId.getName());
            KarooAudioAlertHook.triggerLowBatteryAudioAlert(context.getSdkContext());
            LowBatteryNotification.showLowBatteryNotification(context.getSdkContext(), deviceId.getName(), batteryInfo.getValue());

            if (!karooNotificationResult) {
                Toast toast = Toast.makeText(context.getSdkContext(), context.getSdkContext().getString(R.string.text_param_di2_low_battery, deviceId.getName(), batteryInfo.getValue()), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
            }
        }, 500);
    }

}
