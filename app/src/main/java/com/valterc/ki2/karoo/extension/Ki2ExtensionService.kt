package com.valterc.ki2.karoo.extension

import com.valterc.ki2.BuildConfig
import com.valterc.ki2.data.device.DeviceId
import com.valterc.ki2.karoo.extension.datatypes.GearsDataType
import com.valterc.ki2.karoo.extension.shifting.DefaultShiftingDevice
import com.valterc.ki2.karoo.extension.shifting.ShiftingDevice
import io.hammerhead.karooext.extension.KarooExtension
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.models.Device
import io.hammerhead.karooext.models.DeviceEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

class Ki2ExtensionService : KarooExtension("ki2", BuildConfig.VERSION_NAME) {

    private var serviceJob: Job? = null
    private val extensionContext by lazy {
        return@lazy Ki2ExtensionContext(this)
    }

    override val types by lazy {
        listOf(
            GearsDataType(extension, extensionContext),
        )
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

        when (uid){
            "default" -> DefaultShiftingDevice(extension, extensionContext).connect(emitter)
            else -> ShiftingDevice(extension, extensionContext, DeviceId(uid)).connect(emitter)
        }
    }

    override fun onDestroy() {
        serviceJob?.cancel()
        serviceJob = null

        extensionContext.serviceClient.dispose()

        super.onDestroy()
    }
}