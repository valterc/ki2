package com.valterc.ki2.services.karoo

import android.content.Context
import com.valterc.ki2.data.ride.RideStatus
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.models.RideState
import timber.log.Timber
import java.util.function.Consumer

/**
 * Listens to Karoo system ride events (start, pause, resume, end) using a
 * [KarooSystemService] connection and forwards them, translated into the
 * internal [RideStatus] representation, to the provided consumer.
 *
 */
class KarooRideStateListener(
    context: Context,
    private val rideStatusConsumer: Consumer<RideStatus>
) {

    private val karooSystem: KarooSystemService = KarooSystemService(context)
    private var consumerId: String? = null
    private var rideState: RideState = RideState.Idle

    /**
     * Connect to the Karoo system and start listening for ride state changes.
     */
    fun start() {
        karooSystem.connect { connected ->
            Timber.i("Karoo ride state listener connected: %s", connected)
            if (connected && consumerId == null) {
                consumerId = karooSystem.addConsumer { newRideState: RideState ->
                    onRideStateChanged(newRideState)
                }
            }
        }
    }

    private fun onRideStateChanged(newRideState: RideState) {
        val oldRideState = rideState
        rideState = newRideState

        val rideStatus: RideStatus? = when {
            newRideState is RideState.Recording && oldRideState is RideState.Idle -> RideStatus.ONGOING
            newRideState is RideState.Recording && oldRideState is RideState.Paused -> RideStatus.ONGOING
            newRideState is RideState.Paused -> RideStatus.PAUSED
            newRideState is RideState.Idle &&
                    (oldRideState is RideState.Recording || oldRideState is RideState.Paused) -> RideStatus.FINISHED
            else -> null
        }

        if (rideStatus != null) {
            Timber.i("Karoo ride state changed to %s (status=%s)", newRideState, rideStatus)
            rideStatusConsumer.accept(rideStatus)
        }
    }

    /**
     * Stop listening and disconnect from the Karoo system.
     */
    fun dispose() {
        consumerId?.let { karooSystem.removeConsumer(it) }
        consumerId = null
        karooSystem.disconnect()
    }
}

