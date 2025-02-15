package com.valterc.ki2.karoo.shifting

import com.valterc.ki2.data.device.DeviceId
import com.valterc.ki2.data.shifting.ShiftingInfo
import com.valterc.ki2.karoo.Ki2ExtensionContext
import com.valterc.ki2.karoo.RideHandler
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.function.BiConsumer
import java.util.function.Consumer
import kotlin.math.abs

class ShiftCountHandler(extensionContext: Ki2ExtensionContext) : RideHandler(extensionContext) {

    private var previousShiftingInfo: ShiftingInfo? = null
    private val listeners: MutableList<Consumer<ShiftCountHandler>> = mutableListOf()

    var frontShiftCount = 0
        private set
    var rearShiftCount = 0
        private set
    val shiftCount
        get() = frontShiftCount + rearShiftCount

    val shiftingInfoConsumer =
        BiConsumer<DeviceId, ShiftingInfo> { _: DeviceId, shiftingInfo: ShiftingInfo ->
            if (previousShiftingInfo == null) {
                previousShiftingInfo = shiftingInfo
                return@BiConsumer
            }

            val lastShiftingInfo = previousShiftingInfo ?: return@BiConsumer

            frontShiftCount += abs(lastShiftingInfo.frontGear - shiftingInfo.frontGear)
            rearShiftCount += abs(lastShiftingInfo.rearGear - shiftingInfo.rearGear)

            previousShiftingInfo = shiftingInfo

            listeners.forEach { it.accept(this) }
        }

    override fun onRideStart() {
        super.onRideStart()

        extensionContext.serviceClient.registerShiftingInfoWeakListener(
            shiftingInfoConsumer
        )
    }

    override fun onRideEnd() {
        extensionContext.serviceClient.unregisterShiftingInfoWeakListener(
            shiftingInfoConsumer
        )

        frontShiftCount = 0
        rearShiftCount = 0
    }

    fun addListener(listener: Consumer<ShiftCountHandler>) {
        listeners.add(listener)
    }

    fun removeListener(listener: Consumer<ShiftCountHandler>) {
        listeners.remove(listener)
    }

    fun stream(): Flow<ShiftCountHandler> {
        return callbackFlow {

            val listener = Consumer<ShiftCountHandler> { handler: ShiftCountHandler ->
                trySend(handler)
            }

            addListener(listener)
            awaitClose {
                removeListener(listener)
            }
        }
    }

}