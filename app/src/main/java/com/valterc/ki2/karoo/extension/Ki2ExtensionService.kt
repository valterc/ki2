package com.valterc.ki2.karoo.extension

import com.valterc.ki2.BuildConfig
import com.valterc.ki2.data.device.DeviceId
import io.hammerhead.karooext.extension.KarooExtension
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.models.Device
import io.hammerhead.karooext.models.DeviceEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.function.Consumer

class Ki2ExtensionService : KarooExtension("ki2", BuildConfig.VERSION_NAME) {

    private var serviceJob: Job? = null
    private var extensionContext: Ki2ExtensionContext? = null

    override fun onCreate() {
        super.onCreate()
        extensionContext = Ki2ExtensionContext(this)
    }

    override fun startScan(emitter: Emitter<Device>) {
        val flow = callbackFlow {
            val listener = Consumer<DeviceId> {
                val device = ShiftingDevice(extension, it).source
                Timber.i("Sending device %s", device)
                trySend(device)
            }

            Timber.i("Starting device scan")
            extensionContext?.serviceClient?.startDeviceScan(listener)

            awaitClose {
                Timber.i("Stopped device scan")
                extensionContext?.serviceClient?.stopDeviceScan(listener)
            }
        }

        val job = CoroutineScope(Dispatchers.IO).launch {
            flow.collect{
                emitter.onNext(it)
            }
        }

        emitter.setCancellable {
            job.cancel()
        }
    }

    override fun connectDevice(uid: String, emitter: Emitter<DeviceEvent>) {
        Timber.i("On device connect %s", uid)
        ShiftingDevice(extension, DeviceId(uid)).connect(emitter)
    }

    override fun onDestroy() {
        serviceJob?.cancel()
        serviceJob = null

        extensionContext?.serviceClient?.dispose()

        super.onDestroy()
    }
}