package com.valterc.ki2.karoo

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import com.valterc.ki2.data.connection.ConnectionDataInfo
import com.valterc.ki2.data.device.DeviceId
import com.valterc.ki2.data.action.KarooActionEvent
import com.valterc.ki2.data.message.Message
import com.valterc.ki2.services.IKi2Service
import com.valterc.ki2.services.Ki2Service
import com.valterc.ki2.services.callbacks.IConnectionDataInfoCallback
import com.valterc.ki2.services.callbacks.IActionCallback
import com.valterc.ki2.services.callbacks.IMessageCallback
import com.valterc.ki2.services.callbacks.IScanCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn

class Ki2ServiceConnection(private val context: Context) {

    private var ki2Service: IKi2Service? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            ki2Service = IKi2Service.Stub.asInterface(service)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            ki2Service = null;
        }

        override fun onBindingDied(name: ComponentName) {
            ki2Service = null;
            context.unbindService(this)
            connect()
        }
    }

    private fun deviceScanFlow() = callbackFlow {
        val listener = object : IScanCallback.Default() {
            override fun onScanResult(deviceId: DeviceId?) {
                deviceId?.let { trySend(it) }
            }
        }

        ki2Service?.registerScanListener(listener)

        awaitClose {
            ki2Service?.unregisterScanListener(listener)
        }
    }

    private val deviceDataFlow = callbackFlow {
        val listener = object : IConnectionDataInfoCallback.Default() {
            override fun onConnectionDataInfo(
                deviceId: DeviceId?,
                connectionDataInfo: ConnectionDataInfo?
            ) {
                connectionDataInfo?.let {
                    trySend(connectionDataInfo)
                }
            }
        }

        ki2Service?.registerConnectionDataInfoListener(listener)

        awaitClose {
            ki2Service?.unregisterConnectionDataInfoListener(listener)
        }
    }

    fun deviceDataFlow(): SharedFlow<ConnectionDataInfo> =
        deviceDataFlow.shareIn(CoroutineScope(Dispatchers.IO), SharingStarted.Lazily)

    private val inputKeyFlow = callbackFlow {
        val listener = object : IActionCallback.Default() {
            override fun onActionEvent(deviceId: DeviceId?, actionEvent: KarooActionEvent?) {
                actionEvent?.let {
                    trySend(actionEvent)
                }
            }
        }

        ki2Service?.registerActionListener(listener)

        awaitClose {
            ki2Service?.unregisterActionListener(listener)
        }
    }

    fun inputKeyFlow(): SharedFlow<KarooActionEvent> =
        inputKeyFlow.shareIn(CoroutineScope(Dispatchers.IO), SharingStarted.Lazily)

    private val messageFlow = callbackFlow {
        val listener = object : IMessageCallback.Default() {
            override fun onMessage(message: Message?) {
                message?.let {
                    trySend(message)
                }
            }
        }

        ki2Service?.registerMessageListener(listener)

        awaitClose {
            ki2Service?.unregisterMessageListener(listener)
        }
    }

    fun messageFlow(): SharedFlow<Message> =
        messageFlow.shareIn(CoroutineScope(Dispatchers.IO), SharingStarted.Lazily)

    /**
     * Connect to KarooSystem
     */
    fun connect(
        /**
         * Callback for when Karoo system connects and disconnects
         */
        onConnection: ((Boolean) -> Unit)? = null,
    ) {
        val intent = Ki2Service.getIntent()
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        onConnection?.let {
        }
    }

}