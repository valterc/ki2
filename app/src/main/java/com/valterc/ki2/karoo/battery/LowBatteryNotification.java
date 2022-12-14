package com.valterc.ki2.karoo.battery;

import android.content.Context;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.drawable.IconCompat;

import com.valterc.ki2.R;
import com.valterc.ki2.utils.DrawableUtils;

public class LowBatteryNotification {

    private static final String EXTRA_HEADER = "io.hammerhead.notification.header";
    private static final String EXTRA_ACTION = "io.hammerhead.notification.action";
    private static final String CHANNEL_ID = "ki2-battery";

    private LowBatteryNotification() {
    }

    public static void showLowBatteryNotification(Context context, String deviceName, LowBatteryCategory category, int batteryPercentage) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        String lowBatteryString = context.getString(R.string.text_param_di2_low_battery, deviceName, batteryPercentage);

        //notificationManager.create
        //NotificationChannelCompat.Builder channelBuilder = new NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT);
        //channelBuilder.setName(context.getString(R.string.text_di2_low_battery));

        //NotificationChannelCompat notificationChannel = channelBuilder.build();
        //notificationManager.createNotificationChannel(notificationChannel);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, "notificationChannel.getId()")
                .setSmallIcon(R.drawable.ic_battery_0)
                .setOngoing(false)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(lowBatteryString)
                .setContentText(lowBatteryString)
                .setCategory(category.getNotificationCategory())
                .setGroup(deviceName)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(lowBatteryString));

        notification.getExtras().putString(EXTRA_HEADER, context.getString(R.string.text_di2_low_battery));
        notification.getExtras().putString(EXTRA_ACTION, context.getString(R.string.text_dismiss));

        notificationManager.notify(deviceName.hashCode(), notification.build());
    }

}
