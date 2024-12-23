package com.valterc.ki2.karoo.extension

import io.hammerhead.karooext.models.RideState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class RideHandler(protected val extensionContext: Ki2ExtensionContext) {
    protected var rideState: RideState? = null

    init {
        CoroutineScope(Dispatchers.IO).launch {
            extensionContext.karooSystem.streamRideState().collect{ newRideState: RideState ->
                val oldRideState = rideState
                rideState = newRideState

                when {
                    newRideState is RideState.Recording && (oldRideState == null || oldRideState is RideState.Idle) -> onRideStart()
                    newRideState is RideState.Recording && oldRideState is RideState.Paused -> onRideResume()
                    newRideState is RideState.Paused -> onRidePause()
                    newRideState is RideState.Idle && (oldRideState is RideState.Recording || oldRideState is RideState.Paused) -> onRideEnd()
                }
            }
        }
    }

    protected open fun onRideStart() {}

    protected open fun onRidePause() {}

    protected open fun onRideResume() {}

    protected open fun onRideEnd() {}

}