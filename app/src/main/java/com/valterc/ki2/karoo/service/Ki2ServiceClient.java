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
import com.valterc.ki2.services.IKi2Service;
import com.valterc.ki2.services.Ki2Service;
import com.valterc.ki2.services.callbacks.IBatteryCallback;
import com.valterc.ki2.services.callbacks.IConnectionInfoCallback;
import com.valterc.ki2.services.callbacks.IKeyCallback;
import com.valterc.ki2.services.callbacks.IMessageCallback;
import com.valterc.ki2.services.callbacks.IShiftingCallback;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.hammerhead.sdk.v0.SdkContext;

@SuppressLint("LogNotTimber")
public class Ki2ServiceClient {

    @SuppressWarnings("FieldCanBeLocal")
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = IKi2Service.Stub.asInterface(binder);
            handler.post(() -> {
                maybeStartMessageEvents();
                maybeStartKeyEvents();
                maybeStartBatteryEvents();
                maybeStartConnectionEvents();
                maybeStartShiftingEvents();
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            service = null;
        }
    };

    private final IConnectionInfoCallback connectionInfoCallback = new IConnectionInfoCallback.Stub() {
        @Override
        public void onConnectionInfo(DeviceId deviceId, ConnectionInfo connectionInfo) {
            handler.post(() -> {
                connectionInfoListeners.pushData(deviceId, connectionInfo);
                maybeStopConnectionEvents();
            });
        }
    };

    private final IBatteryCallback batteryCallback = new IBatteryCallback.Stub() {
        @Override
        public void onBattery(DeviceId deviceId, BatteryInfo batteryInfo) {
            handler.post(() -> {
                batteryInfoListeners.pushData(deviceId, batteryInfo);
                maybeStopBatteryEvents();
            });
        }
    };

    private final IShiftingCallback shiftingCallback = new IShiftingCallback.Stub() {
        @Override
        public void onShifting(DeviceId deviceId, ShiftingInfo shiftingInfo) {
            handler.post(() -> {
                shiftingInfoListeners.pushData(deviceId, shiftingInfo);
                maybeStopShiftingEvents();
            });
        }
    };

    private final IKeyCallback keyCallback = new IKeyCallback.Stub() {
        @Override
        public void onKeyEvent(DeviceId deviceId, KarooKeyEvent keyEvent) {
            handler.post(() -> {
                try {
                    inputAdapter.executeKeyEvent(keyEvent);
                } catch (Exception e) {
                    Log.e("KI2", "Error during callback", e);
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

    private final InputAdapter inputAdapter;
    private final Handler handler;
    private final BiDataStreamWeakListenerList<DeviceId, ConnectionInfo> connectionInfoListeners;
    private final BiDataStreamWeakListenerList<DeviceId, BatteryInfo> batteryInfoListeners;
    private final BiDataStreamWeakListenerList<DeviceId, ShiftingInfo> shiftingInfoListeners;
    private final DataStreamWeakListenerList<Message> messageListeners;
    private IKi2Service service;

    public Ki2ServiceClient(SdkContext context) {
        inputAdapter = new InputAdapter(context);
        connectionInfoListeners = new BiDataStreamWeakListenerList<>();
        batteryInfoListeners = new BiDataStreamWeakListenerList<>();
        shiftingInfoListeners = new BiDataStreamWeakListenerList<>();
        messageListeners = new DataStreamWeakListenerList<>();
        handler = new Handler(Looper.getMainLooper());
        context.bindService(Ki2Service.getIntent(), serviceConnection, Context.BIND_AUTO_CREATE);
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

    private void maybeStartMessageEvents(){
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
            return new PreferencesView();
        }

        try {
            return service.getPreferences();
        } catch (Exception e) {
            Log.e("KI2", "Unable to get preferences", e);
        }

        return new PreferencesView();
    }

}
