package com.valterc.ki2.karoo.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.drawable.Icon;

import androidx.appcompat.content.res.AppCompatResources;

import com.valterc.ki2.R;
import com.valterc.ki2.utils.DrawableUtils;

public class LowBatteryNotification {

    private static final String EXTRA_HEADER = "io.hammerhead.notification.header";
    private static final String EXTRA_ACTION = "io.hammerhead.notification.action";

    private static final String GROUP_NAME = "sensor";

    private static final int NOTIFICATION_ID = 0x7700;

    private LowBatteryNotification() {
    }

    public static void showLowBatteryNotification(Context context, String deviceName, int batteryPercentage) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String lowBatteryString = context.getString(R.string.text_param_di2_low_battery, deviceName, batteryPercentage);

        NotificationChannel notificationChannel = new NotificationChannel("ki2-battery", context.getString(R.string.text_di2_low_battery), NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannel.enableVibration(true);
        notificationChannel.setVibrationPattern(new long[] { 1000, 1000 });
        notificationManager.createNotificationChannel(notificationChannel);

        Notification.Builder notification = new Notification.Builder(context, notificationChannel.getId())
                .setSmallIcon(Icon.createWithBitmap(DrawableUtils.drawableToBitmap(AppCompatResources.getDrawable(context, R.drawable.ic_battery_0))))
                .setOngoing(false)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(lowBatteryString)
                .setContentText(lowBatteryString)
                .setCategory(Notification.CATEGORY_EVENT)
                .setGroup(GROUP_NAME)
                .setStyle(new Notification.BigTextStyle().bigText(lowBatteryString));

        notification.getExtras().putString(EXTRA_HEADER, context.getString(R.string.text_di2_low_battery));
        notification.getExtras().putString(EXTRA_ACTION, context.getString(R.string.text_dismiss));

        notificationManager.notify(NOTIFICATION_ID, notification.build());
    }

}
