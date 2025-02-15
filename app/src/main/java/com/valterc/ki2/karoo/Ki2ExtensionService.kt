package com.valterc.ki2.karoo

import android.content.ComponentName
import android.content.Intent
import com.valterc.ki2.BuildConfig
import com.valterc.ki2.data.device.DeviceId
import com.valterc.ki2.karoo.datatypes.text.FrontGearIndexDataType
import com.valterc.ki2.karoo.datatypes.text.FrontGearSizeDataType
import com.valterc.ki2.karoo.datatypes.text.GearRatioDataType
import com.valterc.ki2.karoo.datatypes.text.GearsIndexDataType
import com.valterc.ki2.karoo.datatypes.text.GearsSizeDataType
import com.valterc.ki2.karoo.datatypes.text.RearGearIndexDataType
import com.valterc.ki2.karoo.datatypes.text.RearGearSizeDataType
import com.valterc.ki2.karoo.datatypes.text.ShiftCountDataType
import com.valterc.ki2.karoo.datatypes.text.ShiftingBatteryPercentageDataType
import com.valterc.ki2.karoo.datatypes.text.ShiftingModeDataType
import com.valterc.ki2.karoo.datatypes.visual.BikeBatteryVisualDataType
import com.valterc.ki2.karoo.datatypes.visual.DrivetrainIndexVisualDataType
import com.valterc.ki2.karoo.datatypes.visual.DrivetrainSizeVisualDataType
import com.valterc.ki2.karoo.datatypes.visual.GearsDetailedVisualDataType
import com.valterc.ki2.karoo.datatypes.visual.GearsIndexVisualDataType
import com.valterc.ki2.karoo.datatypes.visual.GearsSizeVisualDataType
import com.valterc.ki2.karoo.overlay.OverlayWindowHandler
import com.valterc.ki2.karoo.shifting.ShiftingAudioAlertHandler
import com.valterc.ki2.karoo.shifting.ShiftingDevice
import com.valterc.ki2.karoo.update.UpdateHandler
import io.hammerhead.karooext.extension.KarooExtension
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.models.Device
import io.hammerhead.karooext.models.DeviceEvent
import io.hammerhead.karooext.models.RequestAnt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class Ki2ExtensionService : KarooExtension("ki2", BuildConfig.VERSION_NAME) {

    companion object {
        fun getIntent(): Intent {
            val serviceIntent = Intent()
            serviceIntent.setComponent(
                ComponentName(
                    "com.valterc.ki2",
                    Ki2ExtensionService::class.java.name
                )
            )
            return serviceIntent
        }
    }

    private val extensionContext by lazy {
        return@lazy Ki2ExtensionContext(extension, this)
    }

    private val handlers = mutableListOf<RideHandler>()

    override val types by lazy {
        listOf(
            ShiftingBatteryPercentageDataType(extensionContext),
            ShiftingModeDataType(extensionContext),
            GearRatioDataType(extensionContext),
            GearsIndexDataType(extensionContext),
            GearsSizeDataType(extensionContext),
            FrontGearIndexDataType(extensionContext),
            FrontGearSizeDataType(extensionContext),
            RearGearIndexDataType(extensionContext),
            RearGearSizeDataType(extensionContext),
            ShiftCountDataType(extensionContext),

            GearsIndexVisualDataType(extensionContext),
            GearsSizeVisualDataType(extensionContext),
            GearsDetailedVisualDataType(extensionContext),
            DrivetrainIndexVisualDataType(extensionContext),
            DrivetrainSizeVisualDataType(extensionContext),
            BikeBatteryVisualDataType(extensionContext),
        )
    }

    override fun onCreate() {
        super.onCreate()
        extensionContext.let {
            Timber.i("Ki2 Extension initialized")

            extensionContext.karooSystem.connect { connected ->
                Timber.i("Connected to Karoo System: $connected")

                extensionContext.karooSystem.dispatch(RequestAnt(extension))
                handlers.add(UpdateHandler(extensionContext))
                handlers.add(OverlayWindowHandler(this, extensionContext))
                handlers.add(ShiftingAudioAlertHandler(extensionContext))
            }
        }
    }

    override fun startScan(emitter: Emitter<Device>) {
        val job = CoroutineScope(Dispatchers.IO).launch {
            delay(1000)
            extensionContext.serviceClient.savedDevices?.let {
                for (device: DeviceId in it) {
                    val shiftingDevice = ShiftingDevice(extensionContext, device).source
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
        ShiftingDevice(extensionContext, DeviceId(uid)).connect(emitter)
    }

    override fun onDestroy() {
        extensionContext.karooSystem.disconnect()
        extensionContext.serviceClient.dispose()
        super.onDestroy()
    }
}