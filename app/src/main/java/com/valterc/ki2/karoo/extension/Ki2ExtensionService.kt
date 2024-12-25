package com.valterc.ki2.karoo.extension

import android.content.ComponentName
import android.content.Intent
import com.valterc.ki2.BuildConfig
import com.valterc.ki2.data.device.DeviceId
import com.valterc.ki2.karoo.extension.datatypes.GearsDataType
import com.valterc.ki2.karoo.extension.overlay.OverlayWindowHandler
import com.valterc.ki2.karoo.extension.shifting.ShiftingAudioAlertHandler
import com.valterc.ki2.karoo.extension.shifting.ShiftingDevice
import io.hammerhead.karooext.extension.KarooExtension
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.models.Device
import io.hammerhead.karooext.models.DeviceEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

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

    private val extensionContext by lazy {
        return@lazy Ki2ExtensionContext(this)
    }

    private val handlers = mutableListOf<RideHandler>()

    override val types by lazy {
        listOf(
            GearsDataType(extension, extensionContext),
        )
    }

    override fun onCreate() {
        super.onCreate()
        extensionContext.let {
            Timber.i("Ki2 Extension initialized")

            extensionContext.karooSystem.connect { connected ->
                Timber.i("Connected to Karoo System: $connected")
            }

            handlers.add(OverlayWindowHandler(this, extensionContext))
            handlers.add(ShiftingAudioAlertHandler(extensionContext))
        }
    }

    override fun startScan(emitter: Emitter<Device>) {
        val job = CoroutineScope(Dispatchers.IO).launch {
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
        ShiftingDevice(extension, extensionContext, DeviceId(uid)).connect(emitter)
    }

    override fun onDestroy() {
        extensionContext.karooSystem.disconnect()
        extensionContext.serviceClient.dispose()
        super.onDestroy()
    }
}