package com.valterc.ki2.karoo.overlay

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service.ACTIVITY_SERVICE
import android.app.Service.NOTIFICATION_SERVICE
import android.app.Service.RECEIVER_EXPORTED
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.valterc.ki2.R
import com.valterc.ki2.data.preferences.PreferencesView
import com.valterc.ki2.karoo.Ki2ExtensionContext
import com.valterc.ki2.karoo.Ki2ExtensionService
import com.valterc.ki2.karoo.RideHandler
import com.valterc.ki2.karoo.overlay.manager.OverlayManager
import io.hammerhead.karooext.models.RideState
import io.hammerhead.karooext.models.SystemNotification
import timber.log.Timber
import java.util.function.Consumer

@SuppressLint("InflateParams")
class OverlayWindowHandler(
    private val service: Ki2ExtensionService,
    extensionContext: Ki2ExtensionContext
) : RideHandler(extensionContext) {
    companion object {
        private const val NOTIFICATION_ID_OVERLAY = 0x101
        private const val NOTIFICATION_ID_PERMISSION = "102"
    }

    private var overlayManager: OverlayManager? = null
    private var overlayEnabled = false
    private var inRideApp = false

    @Suppress("DEPRECATION")
    private fun isForeground() =
        (service.getSystemService(ACTIVITY_SERVICE) as? ActivityManager)
            ?.getRunningServices(Integer.MAX_VALUE)
            ?.find { it.service.className == javaClass.name}
            ?.foreground == true

    private val preferencesListener = Consumer<PreferencesView> { preferences: PreferencesView ->
        overlayEnabled = preferences.isOverlayEnabled(service)
        if (overlayEnabled) {
            open()
        } else {
            close()
        }
    }

    private val receiverRideAppOpened: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Timber.d("Received Ride App Open")
            inRideApp = true
            open()
        }
    }

    private val receiverRideAppClosed: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Timber.d("Received Ride Stop")
            inRideApp = false
            close()
        }
    }

    init {
        extensionContext.serviceClient.registerPreferencesWeakListener(preferencesListener)
        extensionContext.karooSystem.addConsumer { rideState: RideState ->
            if (rideState is RideState.Recording) {
                inRideApp = true
                open()
            }
        }

        service.registerReceiver(
            receiverRideAppOpened,
            IntentFilter("io.hammerhead.intent.action.RIDE_APP_OPENED"),
            RECEIVER_EXPORTED
        )

        service.registerReceiver(
            receiverRideAppClosed,
            IntentFilter("io.hammerhead.hx.intent.action.RIDE_STOP"),
            RECEIVER_EXPORTED
        )
    }

    private fun open(){
        if (overlayManager != null) {
            return
        }

        if (!overlayEnabled || !inRideApp) {
            return
        }

        if (!Settings.canDrawOverlays(extensionContext.context)) {
            extensionContext.karooSystem.dispatch(SystemNotification(
                NOTIFICATION_ID_PERMISSION,
                "Permission needed for overlay",
                "Enable Ki2 to draw over other apps",
                style = SystemNotification.Style.ERROR,
                actionIntent = Settings.ACTION_MANAGE_OVERLAY_PERMISSION
            ))
            return
        }

        val notificationChannel = NotificationChannel(
            "foreground-service",
            "Foreground Service",
            NotificationManager.IMPORTANCE_MIN
        )
        val notificationManager =
            checkNotNull(service.getSystemService(NOTIFICATION_SERVICE) as NotificationManager?)
        notificationManager.createNotificationChannel(notificationChannel)

        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(service, notificationChannel.id)
        val notification: Notification = notificationBuilder.setOngoing(true)
            .setContentTitle("Ki2")
            .setContentText("Overlay active")
            .setSmallIcon(R.drawable.ic_icon)
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()

        service.startForeground(NOTIFICATION_ID_OVERLAY, notification)

        overlayManager = OverlayManager(extensionContext)
    }

    private fun close() {
        overlayManager = null

        val notificationManager =
            checkNotNull(service.getSystemService(NOTIFICATION_SERVICE) as NotificationManager?)
        notificationManager.cancel(NOTIFICATION_ID_OVERLAY)
    }
}