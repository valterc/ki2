package com.valterc.ki2.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.valterc.ki2.data.device.DeviceId
import timber.log.Timber

@Suppress("DEPRECATION")
class ChangeShiftModeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        try {
            val serviceClient = BroadcastReceiverServiceClient(context, this)
            val deviceId = intent.extras?.getParcelable<DeviceId>(DEVICE_ID_KEY)
            Timber.i("Received intent to change shift mode for device: %s", deviceId)
            deviceId?.let { deviceId ->
                serviceClient.changeShiftMode(deviceId)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error when receiving broadcast to change shift mode")
        }
    }

    companion object {
        const val DEVICE_ID_KEY = "DeviceId"

        fun getIntent(context: Context, deviceId: DeviceId): Intent {
            return Intent(context, ChangeShiftModeReceiver::class.java).apply {
                putExtra(DEVICE_ID_KEY, deviceId)
            }
        }
    }
}