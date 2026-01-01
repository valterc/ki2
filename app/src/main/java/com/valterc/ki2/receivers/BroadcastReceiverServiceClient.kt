package com.valterc.ki2.receivers

import android.content.BroadcastReceiver
import android.content.Context
import com.valterc.ki2.data.device.DeviceId
import com.valterc.ki2.services.IKi2Service
import com.valterc.ki2.services.Ki2Service
import timber.log.Timber

class BroadcastReceiverServiceClient {

    private var service: IKi2Service? = null

    constructor(context: Context, broadcastReceiver: BroadcastReceiver) {
        val binder = broadcastReceiver.peekService(context, Ki2Service.getIntent())
        service = IKi2Service.Stub.asInterface(binder)
        Timber.i("Peek service: %s", service != null)
    }

    fun changeShiftMode(deviceId: DeviceId): Boolean {
        service?.let { service ->
            try {
                service.changeShiftMode(deviceId)
                return true
            } catch (e: Exception) {
                Timber.e(e, "Error changing shift mode for device %s", deviceId)
            }
        }

        return false
    }
}