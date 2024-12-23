package com.valterc.ki2.karoo.extension

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.valterc.ki2.BuildConfig
import com.valterc.ki2.R
import com.valterc.ki2.data.device.DeviceId
import com.valterc.ki2.data.preferences.PreferencesView
import com.valterc.ki2.karoo.extension.datatypes.GearsDataType
import com.valterc.ki2.karoo.extension.overlay.OverlayWindow
import com.valterc.ki2.karoo.extension.shifting.DefaultShiftingDevice
import com.valterc.ki2.karoo.extension.shifting.ShiftingDevice
import io.hammerhead.karooext.extension.KarooExtension
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.models.Device
import io.hammerhead.karooext.models.DeviceEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.function.Consumer

class Ki2ExtensionService : KarooExtension("ki2", BuildConfig.VERSION_NAME) {

    companion object {
        fun getIntent(): Intent {
            val serviceIntent = Intent()
            serviceIntent.setComponent(
                ComponentName(
                    "com.valterc.ki2",
                    "com.valterc.ki2.karoo.extension.Ki2ExtensionService"
                )
            )
            return serviceIntent
        }
    }

    private var overlayWindow: OverlayWindow? = null;

    private val extensionContext by lazy {
        return@lazy Ki2ExtensionContext(this)
    }

    @Suppress("DEPRECATION")
    private fun isForeground() =
        (getSystemService(ACTIVITY_SERVICE) as? ActivityManager)
            ?.getRunningServices(Integer.MAX_VALUE)
            ?.find { it.service.className == javaClass.name}
            ?.foreground == true

    private val preferencesListener = Consumer<PreferencesView> { preferences: PreferencesView ->
        if (preferences.isOverlayEnabled(this)) {
            Timber.e(" --- Creating overlay window ---")

            if (isForeground()) {
                return@Consumer
            }

            if (!Settings.canDrawOverlays(extensionContext.context)) {
                // TODO Show notification with action to enable overlay
                return@Consumer
            }

            val notificationChannel = NotificationChannel(
                "foreground-service",
                "Foreground Service",
                NotificationManager.IMPORTANCE_MIN
            )
            val notificationManager =
                checkNotNull(getSystemService(NOTIFICATION_SERVICE) as NotificationManager?)
            notificationManager.createNotificationChannel(notificationChannel)

            val notificationBuilder: NotificationCompat.Builder =
                NotificationCompat.Builder(this, notificationChannel.id)
            val notification: Notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Ki2")
                .setContentText("Overlay active")
                .setSmallIcon(R.drawable.ic_icon)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()

            startForeground(1, notification)

            Timber.e(" --- Creating overlay window ---")
            overlayWindow = OverlayWindow(extensionContext)
            overlayWindow?.open()
        } else {
            val notificationManager =
                checkNotNull(getSystemService(NOTIFICATION_SERVICE) as NotificationManager?)
            notificationManager.cancel(1)

            overlayWindow?.close()
            overlayWindow = null
        }
    }

    override val types by lazy {
        listOf(
            GearsDataType(extension, extensionContext),
        )
    }

    override fun onCreate() {
        super.onCreate()
        extensionContext.let {
            Timber.i("Ki2 Extension initialized")
        }

        extensionContext.serviceClient.registerPreferencesWeakListener(preferencesListener)
    }

    override fun startScan(emitter: Emitter<Device>) {
        val job = CoroutineScope(Dispatchers.IO).launch {
            emitter.onNext(DefaultShiftingDevice(extension, extensionContext).source)

            extensionContext.serviceClient.savedDevices?.let {
                for (device: DeviceId in it) {
                    val shiftingDevice = ShiftingDevice(extension, extensionContext, device).source
                    emitter.onNext(shiftingDevice)
                }
            }
        }

        emitter.setCancellable {
            job.cancel()
        }
    }

    override fun connectDevice(uid: String, emitter: Emitter<DeviceEvent>) {
        Timber.i("[%s] Device connect", uid)

        when (uid) {
            "default" -> DefaultShiftingDevice(extension, extensionContext).connect(emitter)
            else -> ShiftingDevice(extension, extensionContext, DeviceId(uid)).connect(emitter)
        }
    }

    override fun onDestroy() {
        extensionContext.serviceClient.dispose()
        super.onDestroy()
    }
}