package com.valterc.ki2.karoo.update;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;

import androidx.appcompat.content.res.AppCompatResources;

import com.valterc.ki2.R;
import com.valterc.ki2.data.update.ReleaseInfo;
import com.valterc.ki2.utils.DrawableUtils;

public class UpdateAvailableNotification {

    private static final String EXTRA_HEADER = "io.hammerhead.notification.header";
    private static final String EXTRA_ACTION = "io.hammerhead.notification.action";
    private static final String CHANNEL_ID = "ki2-update";

    private UpdateAvailableNotification() {
    }

    public static void showUpdateAvailableNotification(Context context, ReleaseInfo releaseInfo) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String message = context.getString(R.string.text_param_update_available_version, releaseInfo.getName());

        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, context.getString(R.string.text_update_ki2), NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(notificationChannel);

        Intent intent = new Intent("com.valterc.ki2.action.UPDATE");
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                0,
                intent,
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S ? PendingIntent.FLAG_MUTABLE : 0);

        Notification.Builder notification = new Notification.Builder(context, notificationChannel.getId())
                .setSmallIcon(Icon.createWithBitmap(DrawableUtils.drawableToBitmap(AppCompatResources.getDrawable(context, R.drawable.ic_update))))
                .setOngoing(false)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(message)
                .setContentText(message)
                .setCategory(Notification.CATEGORY_EVENT)
                .setGroup(CHANNEL_ID)
                .setStyle(new Notification.BigTextStyle().bigText(message))
                .setContentIntent(pendingIntent);

        notification.getExtras().putString(EXTRA_HEADER, context.getString(R.string.text_update_ki2));
        notification.getExtras().putString(EXTRA_ACTION, context.getString(R.string.text_update));

        notificationManager.notify(CHANNEL_ID.hashCode(), notification.build());
    }

    public static void clearUpdateAvailableNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(CHANNEL_ID.hashCode());
    }

}
