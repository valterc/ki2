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
    private static final String CHANNEL_ID = "ki2-battery";

    private LowBatteryNotification() {
    }

    public static void showLowBatteryNotification(Context context, String deviceName, LowBatteryCategory category, int batteryPercentage) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String lowBatteryString = context.getString(R.string.text_param_di2_low_battery, deviceName, batteryPercentage);

        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, context.getString(R.string.text_di2_low_battery), NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(notificationChannel);

        Notification.Builder notification = new Notification.Builder(context, notificationChannel.getId())
                .setSmallIcon(Icon.createWithBitmap(DrawableUtils.drawableToBitmap(AppCompatResources.getDrawable(context, R.drawable.ic_battery_0))))
                .setOngoing(false)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(lowBatteryString)
                .setContentText(lowBatteryString)
                .setCategory(category.getNotificationCategory())
                .setGroup(deviceName)
                .setStyle(new Notification.BigTextStyle().bigText(lowBatteryString));

        notification.getExtras().putString(EXTRA_HEADER, context.getString(R.string.text_di2_low_battery));
        notification.getExtras().putString(EXTRA_ACTION, context.getString(R.string.text_dismiss));

        notificationManager.notify(deviceName.hashCode(), notification.build());
    }

}
