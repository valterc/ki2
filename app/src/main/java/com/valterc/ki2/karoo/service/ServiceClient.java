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
import com.valterc.ki2.data.message.Message;
import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.data.preferences.device.DevicePreferencesView;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.karoo.service.device.DeviceDataFrontend;
import com.valterc.ki2.karoo.service.messages.CustomMessageClient;
import com.valterc.ki2.services.IKi2Service;
import com.valterc.ki2.services.Ki2Service;
import com.valterc.ki2.services.callbacks.IMessageCallback;
import com.valterc.ki2.services.callbacks.IPreferencesCallback;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.hammerhead.sdk.v0.SdkContext;

@SuppressLint("LogNotTimber")
public class ServiceClient {

    private static final int TIME_MS_ATTEMPT_BIND = 500;
    private static final int TIME_MS_ATTEMPT_REBIND = 15_000;

    @SuppressWarnings("FieldCanBeLocal")
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = IKi2Service.Stub.asInterface(binder);
            deviceDataFrontend.setService(service);
            handler.post(() -> {
                maybeStartPreferencesEvents();
                maybeStartMessageEvents();
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            service = null;
            deviceDataFrontend.setService(null);
            handler.postDelayed(() -> {
                if (service == null) {
                    Log.w("KI2", "Attempting to re-bind to service");
                    context.unbindService(serviceConnection);
                    attemptBindToService();
                }
            }, TIME_MS_ATTEMPT_REBIND);
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
    private final Handler handler;
    private final CustomMessageClient customMessageClient;
    private final DataStreamWeakListenerList<Message> messageListeners;
    private final DataStreamWeakListenerList<PreferencesView> preferencesListeners;
    private IKi2Service service;
    private final DeviceDataFrontend deviceDataFrontend;

    public ServiceClient(SdkContext context) {
        this.context = context;

        handler = new Handler(Looper.getMainLooper());
        deviceDataFrontend = new DeviceDataFrontend(context, handler);

        messageListeners = new DataStreamWeakListenerList<>();
        preferencesListeners = new DataStreamWeakListenerList<>();
        customMessageClient = new CustomMessageClient(this, handler);

        attemptBindToService();
    }

    private void attemptBindToService() {
        boolean result = context.bindService(Ki2Service.getIntent(), serviceConnection, Context.BIND_AUTO_CREATE);
        if (!result) {
            handler.postDelayed(this::attemptBindToService, (int) (TIME_MS_ATTEMPT_BIND * (1 + 2 * Math.random())));
        }
    }

    public void registerConnectionInfoWeakListener(BiConsumer<DeviceId, ConnectionInfo> connectionInfoConsumer) {
        deviceDataFrontend.registerConnectionInfoWeakListener(connectionInfoConsumer);
    }

    public void registerBatteryInfoWeakListener(BiConsumer<DeviceId, BatteryInfo> batteryInfoConsumer) {
        deviceDataFrontend.registerBatteryInfoWeakListener(batteryInfoConsumer);
    }

    public void registerShiftingInfoWeakListener(BiConsumer<DeviceId, ShiftingInfo> shiftingInfoConsumer) {
        deviceDataFrontend.registerShiftingInfoWeakListener(shiftingInfoConsumer);
    }

    public void registerDevicePreferencesWeakListener(BiConsumer<DeviceId, DevicePreferencesView> devicePreferencesConsumer) {
        deviceDataFrontend.registerDevicePreferencesWeakListener(devicePreferencesConsumer);
    }

    public DevicePreferencesView getDevicePreferences(DeviceId deviceId) {
        return deviceDataFrontend.getDevicePreferences(deviceId);
    }

    public void changeShiftMode(DeviceId deviceId) {
        deviceDataFrontend.changeShiftMode(deviceId);
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
            Log.e("KI2", "Unable to send message", e);
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
