package com.valterc.ki2.karoo.service;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import com.valterc.ki2.data.connection.ConnectionInfo;
import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.input.KarooKeyEvent;
import com.valterc.ki2.data.message.Message;
import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.input.InputAdapter;
import com.valterc.ki2.karoo.service.messages.CustomMessageClient;
import com.valterc.ki2.services.IKi2Service;
import com.valterc.ki2.services.Ki2Service;
import com.valterc.ki2.services.callbacks.IBatteryCallback;
import com.valterc.ki2.services.callbacks.IConnectionInfoCallback;
import com.valterc.ki2.services.callbacks.IKeyCallback;
import com.valterc.ki2.services.callbacks.IMessageCallback;
import com.valterc.ki2.services.callbacks.IPreferencesCallback;
import com.valterc.ki2.services.callbacks.IShiftingCallback;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.hammerhead.sdk.v0.SdkContext;

@SuppressLint("LogNotTimber")
public class ServiceClient {

    private static final int TIME_MS_ATTEMPT_BIND = 500;

    @SuppressWarnings("FieldCanBeLocal")
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = IKi2Service.Stub.asInterface(binder);
            handler.post(() -> {
                maybeStartConnectionEvents();
                maybeStartPreferencesEvents();
                maybeStartMessageEvents();
                maybeStartKeyEvents();
                maybeStartBatteryEvents();
                maybeStartShiftingEvents();
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            service = null;
            connectionFilter.reset();
        }
    };

    private final IConnectionInfoCallback connectionInfoCallback = new IConnectionInfoCallback.Stub() {
        @Override
        public void onConnectionInfo(DeviceId deviceId, ConnectionInfo connectionInfo) {
            handler.post(() -> {
                if (connectionFilter.onConnectionStatusReceived(deviceId, connectionInfo.getConnectionStatus())) {
                    connectionInfoListeners.pushData(deviceId, connectionInfo);

                    if (connectionFilter.shouldEmitEstablishedEvent()) {
                        connectionFilter.emitEstablishedEvent(connectionInfoListeners::pushData);
                    }
                }
                maybeStopConnectionEvents();
            });
        }
    };

    private final IBatteryCallback batteryCallback = new IBatteryCallback.Stub() {
        @Override
        public void onBattery(DeviceId deviceId, BatteryInfo batteryInfo) {
            handler.post(() -> {
                if (connectionFilter.onDataReceived(deviceId)) {
                    batteryInfoListeners.pushData(deviceId, batteryInfo);
                }
                maybeStopBatteryEvents();
            });
        }
    };

    private final IShiftingCallback shiftingCallback = new IShiftingCallback.Stub() {
        @Override
        public void onShifting(DeviceId deviceId, ShiftingInfo shiftingInfo) {
            handler.post(() -> {
                if (connectionFilter.onDataReceived(deviceId)) {
                    shiftingInfoListeners.pushData(deviceId, shiftingInfo);
                }
                maybeStopShiftingEvents();
            });
        }
    };

    private final IKeyCallback keyCallback = new IKeyCallback.Stub() {
        @Override
        public void onKeyEvent(DeviceId deviceId, KarooKeyEvent keyEvent) {
            handler.post(() -> {
                if (connectionFilter.onDataReceived(deviceId)) {
                    try {
                        inputAdapter.executeKeyEvent(keyEvent);
                    } catch (Exception e) {
                        Log.e("KI2", "Error handling input", e);
                    }
                }
                maybeStopKeyEvents();
            });
        }
    };

    private final IMessageCallback messageCallback = new IMessageCallback.Stub() {
        @Override
        public void onMessage(Message message) {
            handler.post(() -> {
                messageListeners.pushData(message, message.isPersistent());
                maybeStopMessageEvents();
            });
        }
    };

    private final IPreferencesCallback preferencesCallback = new IPreferencesCallback.Stub() {
        @Override
        public void onPreferences(PreferencesView preferences) {
            handler.post(() -> {
                preferencesListeners.pushData(preferences);
                maybeStopPreferencesEvents();
            });
        }
    };

    private final Context context;
    private final InputAdapter inputAdapter;
    private final Handler handler;
    private final ConnectionFilter connectionFilter;
    private final CustomMessageClient customMessageClient;
    private final BiDataStreamWeakListenerList<DeviceId, ConnectionInfo> connectionInfoListeners;
    private final BiDataStreamWeakListenerList<DeviceId, BatteryInfo> batteryInfoListeners;
    private final BiDataStreamWeakListenerList<DeviceId, ShiftingInfo> shiftingInfoListeners;
    private final DataStreamWeakListenerList<Message> messageListeners;
    private final DataStreamWeakListenerList<PreferencesView> preferencesListeners;
    private IKi2Service service;

    public ServiceClient(SdkContext context) {
        this.context = context;
        inputAdapter = new InputAdapter(context);
        connectionInfoListeners = new BiDataStreamWeakListenerList<>();
        batteryInfoListeners = new BiDataStreamWeakListenerList<>();
        shiftingInfoListeners = new BiDataStreamWeakListenerList<>();
        messageListeners = new DataStreamWeakListenerList<>();
        preferencesListeners = new DataStreamWeakListenerList<>();
        handler = new Handler(Looper.getMainLooper());
        connectionFilter = new ConnectionFilter();
        customMessageClient = new CustomMessageClient(this);
        attemptBindToService();
    }

    private void attemptBindToService() {
        boolean result = context.bindService(Ki2Service.getIntent(), serviceConnection, Context.BIND_AUTO_CREATE);
        if (!result) {
            handler.postDelayed(this::attemptBindToService, (int) (TIME_MS_ATTEMPT_BIND * (1 + 2 * Math.random())));
        }
    }

    public void registerConnectionInfoWeakListener(BiConsumer<DeviceId, ConnectionInfo> connectionInfoConsumer) {
        handler.post(() -> {
            connectionInfoListeners.addListener(connectionInfoConsumer);
            maybeStartConnectionEvents();
            maybeStartKeyEvents();
        });
    }

    private void maybeStartConnectionEvents() {
        if (service == null) {
            return;
        }

        if (!connectionInfoListeners.hasListeners()) {
            return;
        }

        try {
            service.registerConnectionInfoListener(connectionInfoCallback);
        } catch (RemoteException e) {
            Log.e("KI2", "Unable to register listener", e);
        }
    }

    private void maybeStopConnectionEvents() {
        if (service == null) {
            return;
        }

        if (connectionInfoListeners.hasListeners()) {
            return;
        }

        try {
            service.unregisterConnectionInfoListener(connectionInfoCallback);
        } catch (Exception e) {
            Log.e("KI2", "Unable to unregister listener", e);
        }
    }

    public void registerBatteryInfoWeakListener(BiConsumer<DeviceId, BatteryInfo> batteryInfoConsumer) {
        handler.post(() -> {
            batteryInfoListeners.addListener(batteryInfoConsumer);
            maybeStartBatteryEvents();
            maybeStartKeyEvents();
        });
    }

    private void maybeStartBatteryEvents() {
        if (service == null) {
            return;
        }

        if (!batteryInfoListeners.hasListeners()) {
            return;
        }

        try {
            service.registerBatteryListener(batteryCallback);
        } catch (RemoteException e) {
            Log.e("KI2", "Unable to register listener", e);
        }
    }

    private void maybeStopBatteryEvents() {
        if (service == null) {
            return;
        }

        if (batteryInfoListeners.hasListeners()) {
            return;
        }

        try {
            service.unregisterBatteryListener(batteryCallback);
        } catch (Exception e) {
            Log.e("KI2", "Unable to unregister listener", e);
        }
    }

    public void registerShiftingInfoWeakListener(BiConsumer<DeviceId, ShiftingInfo> shiftingInfoConsumer) {
        handler.post(() -> {
            shiftingInfoListeners.addListener(shiftingInfoConsumer);
            maybeStartShiftingEvents();
            maybeStartKeyEvents();
        });
    }

    private void maybeStartShiftingEvents() {
        if (service == null) {
            return;
        }

        if (!shiftingInfoListeners.hasListeners()) {
            return;
        }

        try {
            service.registerShiftingListener(shiftingCallback);
        } catch (RemoteException e) {
            Log.e("KI2", "Unable to register listener", e);
        }
    }

    private void maybeStopShiftingEvents() {
        if (service == null) {
            return;
        }

        if (shiftingInfoListeners.hasListeners()) {
            return;
        }

        try {
            service.unregisterShiftingListener(shiftingCallback);
        } catch (Exception e) {
            Log.e("KI2", "Unable to unregister listener", e);
        }
    }

    private void maybeStartKeyEvents() {
        if (service == null) {
            return;
        }

        if (!shiftingInfoListeners.hasListeners() &&
                !connectionInfoListeners.hasListeners() &&
                !batteryInfoListeners.hasListeners()) {
            return;
        }

        try {
            service.registerKeyListener(keyCallback);
        } catch (RemoteException e) {
            Log.e("KI2", "Unable to register listener", e);
        }
    }

    private void maybeStopKeyEvents() {
        if (service == null) {
            return;
        }

        if (shiftingInfoListeners.hasListeners() ||
                connectionInfoListeners.hasListeners() ||
                batteryInfoListeners.hasListeners()) {
            return;
        }

        try {
            service.unregisterKeyListener(keyCallback);
        } catch (RemoteException e) {
            Log.e("KI2", "Unable to unregister listener", e);
        }
    }

    public void registerMessageWeakListener(Consumer<Message> messageConsumer) {
        handler.post(() -> {
            messageListeners.addListener(messageConsumer);
            maybeStartMessageEvents();
        });
    }

    public void sendMessage(Message message) {
        if (service == null) {
            return;
        }

        try {
            service.sendMessage(message);
        } catch (RemoteException e) {
            Log.e("KI2", "Unable to send message");
        }
    }

    public CustomMessageClient getCustomMessageClient() {
        return customMessageClient;
    }

    private void maybeStartMessageEvents() {
        if (service == null) {
            return;
        }

        if (!messageListeners.hasListeners()) {
            return;
        }

        try {
            service.registerMessageListener(messageCallback);
        } catch (RemoteException e) {
            Log.e("KI2", "Unable to register listener", e);
        }
    }

    private void maybeStopMessageEvents() {
        if (service == null) {
            return;
        }

        if (messageListeners.hasListeners()) {
            return;
        }

        try {
            service.unregisterMessageListener(messageCallback);
        } catch (Exception e) {
            Log.e("KI2", "Unable to unregister listener", e);
        }
    }

    public PreferencesView getPreferences() {
        if (service == null) {
            return null;
        }

        try {
            return service.getPreferences();
        } catch (Exception e) {
            Log.e("KI2", "Unable to get preferences", e);
        }

        return null;
    }

    public void registerPreferencesWeakListener(Consumer<PreferencesView> preferencesConsumer) {
        handler.post(() -> {
            preferencesListeners.addListener(preferencesConsumer);
            maybeStartPreferencesEvents();
        });
    }

    private void maybeStartPreferencesEvents() {
        if (service == null) {
            return;
        }

        if (!preferencesListeners.hasListeners()) {
            return;
        }

        try {
            service.registerPreferencesListener(preferencesCallback);
        } catch (RemoteException e) {
            Log.e("KI2", "Unable to register listener", e);
        }
    }

    private void maybeStopPreferencesEvents() {
        if (service == null) {
            return;
        }

        if (preferencesListeners.hasListeners()) {
            return;
        }

        try {
            service.unregisterPreferencesListener(preferencesCallback);
        } catch (Exception e) {
            Log.e("KI2", "Unable to unregister listener", e);
        }
    }

}
