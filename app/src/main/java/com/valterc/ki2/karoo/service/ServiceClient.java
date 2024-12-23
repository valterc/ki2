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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.valterc.ki2.BuildConfig;
import com.valterc.ki2.data.action.KarooActionEvent;
import com.valterc.ki2.data.connection.ConnectionInfo;
import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.message.Message;
import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.data.preferences.device.DevicePreferencesView;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.karoo.service.device.DeviceDataFrontend;
import com.valterc.ki2.karoo.service.listeners.DataStreamWeakListenerList;
import com.valterc.ki2.karoo.service.listeners.ServiceCallbackRegistration;
import com.valterc.ki2.karoo.service.messages.CustomMessageClient;
import com.valterc.ki2.services.IKi2Service;
import com.valterc.ki2.services.Ki2Service;
import com.valterc.ki2.services.callbacks.IMessageCallback;
import com.valterc.ki2.services.callbacks.IPreferencesCallback;
import com.valterc.ki2.services.callbacks.IScanCallback;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SuppressLint("LogNotTimber")
public class ServiceClient {

    private static final int TIME_MS_ATTEMPT_BIND = 500;
    private static final int TIME_MS_ATTEMPT_REBIND_DEBUG = 15_000;
    private static final int TIME_MS_ATTEMPT_REBIND_RELEASE = 3_000;
    private static final int TIME_MS_ATTEMPT_REBIND = BuildConfig.DEBUG ? TIME_MS_ATTEMPT_REBIND_DEBUG : TIME_MS_ATTEMPT_REBIND_RELEASE;

    @SuppressWarnings("FieldCanBeLocal")
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = IKi2Service.Stub.asInterface(binder);

            registrationMessage.setUnregistered();
            registrationPreferences.setUnregistered();
            registrationScan.setUnregistered();
            deviceDataFrontend.setService(service);

            handler.post(() -> {
                maybeStartPreferencesEvents();
                maybeStartMessageEvents();
                maybeStartScanEvents();
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            service = null;

            registrationMessage.setUnregistered();
            registrationPreferences.setUnregistered();
            registrationScan.setUnregistered();
            deviceDataFrontend.setService(null);

            if (disposed) {
                return;
            }

            handler.postDelayed(() -> {
                if (service == null) {
                    Log.w("KI2", "Attempting to re-bind to service");
                    context.unbindService(serviceConnection);
                    attemptBindToService();
                }
            }, TIME_MS_ATTEMPT_REBIND);
        }

        @Override
        public void onBindingDied(ComponentName name) {
            service = null;

            registrationMessage.setUnregistered();
            registrationPreferences.setUnregistered();
            registrationScan.setUnregistered();
            deviceDataFrontend.setService(null);

            if (disposed) {
                return;
            }

            handler.postDelayed(() -> {
                if (service == null) {
                    Log.w("KI2", "Attempting to re-bind to service");
                    context.unbindService(serviceConnection);
                    attemptBindToService();
                }
            }, TIME_MS_ATTEMPT_REBIND);
        }
    };

    private IKi2Service service;
    private boolean disposed;

    private final Context context;
    private final Handler handler;
    private final CustomMessageClient customMessageClient;
    private final DataStreamWeakListenerList<Message> messageListeners;
    private final DataStreamWeakListenerList<PreferencesView> preferencesListeners;
    private final DataStreamWeakListenerList<DeviceId> scanListeners;
    private final DeviceDataFrontend deviceDataFrontend;

    private final ServiceCallbackRegistration<IMessageCallback> registrationMessage = new ServiceCallbackRegistration<>(new IMessageCallback.Stub() {
        @Override
        public void onMessage(Message message) {
            handler.post(() -> {
                messageListeners.pushData(message, message.isPersistent());
                maybeStopMessageEvents();
            });
        }
    }, callback -> service.registerMessageListener(callback), callback -> service.unregisterMessageListener(callback));

    private final ServiceCallbackRegistration<IPreferencesCallback> registrationPreferences = new ServiceCallbackRegistration<>(new IPreferencesCallback.Stub() {
        @Override
        public void onPreferences(PreferencesView preferences) {
            handler.post(() -> {
                preferencesListeners.pushData(preferences);
                maybeStopPreferencesEvents();
            });
        }
    }, callback -> service.registerPreferencesListener(callback), callback -> service.unregisterPreferencesListener(callback));

    private final ServiceCallbackRegistration<IScanCallback> registrationScan = new ServiceCallbackRegistration<>(new IScanCallback.Stub() {
        @Override
        public void onScanResult(DeviceId deviceId) {
            handler.post(() -> {
                scanListeners.pushData(deviceId);
                maybeStopPreferencesEvents();
            });
        }
    }, callback -> service.registerScanListener(callback), callback -> service.unregisterScanListener(callback));

    public ServiceClient(Context context) {
        this.context = context;

        handler = new Handler(Looper.getMainLooper());
        messageListeners = new DataStreamWeakListenerList<>();
        preferencesListeners = new DataStreamWeakListenerList<>();
        scanListeners = new DataStreamWeakListenerList<>();
        customMessageClient = new CustomMessageClient(this, handler);
        deviceDataFrontend = new DeviceDataFrontend(context);

        attemptBindToService();
    }

    private void attemptBindToService() {
        boolean result = context.bindService(Ki2Service.getIntent(), serviceConnection, Context.BIND_AUTO_CREATE);
        if (!result) {
            handler.postDelayed(this::attemptBindToService, (int) (TIME_MS_ATTEMPT_BIND * (1 + 2 * Math.random())));
        }
    }

    public void dispose() {
        disposed = true;
        context.unbindService(serviceConnection);
        service = null;
    }

    /**
     * Register a weak referenced listener that will receive action events from the main ride device.
     *
     * @param actionEventConsumer Consumer that will receive action events. It will be referenced using a weak reference so the owner must keep a strong reference.
     */
    public void registerActionEventWeakListener(BiConsumer<DeviceId, KarooActionEvent> actionEventConsumer) {
        deviceDataFrontend.registerActionEventWeakListener(actionEventConsumer);
    }

    /**
     * Unregister a weak referenced listener that will receive action events from the main ride device.
     *
     * @param actionEventConsumer Consumer that will receive action events. It will be referenced using a weak reference so the owner must keep a strong reference.
     */
    public void unregisterActionEventWeakListener(BiConsumer<DeviceId, KarooActionEvent> actionEventConsumer) {
        deviceDataFrontend.unregisterActionEventWeakListener(actionEventConsumer);
    }

    /**
     * Register a weak referenced listener that will receive connection info from the main ride device.
     *
     * @param connectionInfoConsumer Consumer that will receive connection events. It will be referenced using a weak reference so the owner must keep a strong reference.
     */
    public void registerConnectionInfoWeakListener(BiConsumer<DeviceId, ConnectionInfo> connectionInfoConsumer) {
        deviceDataFrontend.registerConnectionInfoWeakListener(connectionInfoConsumer);
    }

    /**
     * Unregister a weak referenced listener that will receive connection info from the main ride device.
     *
     * @param connectionInfoConsumer Consumer that will receive connection events. It will be referenced using a weak reference so the owner must keep a strong reference.
     */
    public void unregisterConnectionInfoWeakListener(BiConsumer<DeviceId, ConnectionInfo> connectionInfoConsumer) {
        deviceDataFrontend.unregisterConnectionInfoWeakListener(connectionInfoConsumer);
    }

    /**
     * Register a weak referenced listener that will receive connection info from all devices.
     *
     * @param connectionInfoConsumer Consumer that will receive connection events. It will be referenced using a weak reference so the owner must keep a strong reference.
     */
    public void registerUnfilteredConnectionInfoWeakListener(BiConsumer<DeviceId, ConnectionInfo> connectionInfoConsumer) {
        deviceDataFrontend.registerUnfilteredConnectionInfoWeakListener(connectionInfoConsumer);
    }

    /**
     * Unregister a weak referenced listener that will receive connection info from all devices.
     *
     * @param connectionInfoConsumer Consumer that will receive connection events. It will be referenced using a weak reference so the owner must keep a strong reference.
     */
    public void unregisterUnfilteredConnectionInfoWeakListener(BiConsumer<DeviceId, ConnectionInfo> connectionInfoConsumer) {
        deviceDataFrontend.unregisterUnfilteredConnectionInfoWeakListener(connectionInfoConsumer);
    }

    /**
     * Register a weak referenced listener that will receive battery info from the main ride device.
     *
     * @param batteryInfoConsumer Consumer that will receive battery events. It will be referenced using a weak reference so the owner must keep a strong reference.
     */
    public void registerBatteryInfoWeakListener(BiConsumer<DeviceId, BatteryInfo> batteryInfoConsumer) {
        deviceDataFrontend.registerBatteryInfoWeakListener(batteryInfoConsumer);
    }

    /**
     * Unregister a weak referenced listener that will receive battery info from the main ride device.
     *
     * @param batteryInfoConsumer Consumer that will receive battery events. It will be referenced using a weak reference so the owner must keep a strong reference.
     */
    public void unregisterBatteryInfoWeakListener(BiConsumer<DeviceId, BatteryInfo> batteryInfoConsumer) {
        deviceDataFrontend.unregisterBatteryInfoWeakListener(batteryInfoConsumer);
    }

    /**
     * Register a weak referenced listener that will receive battery info from the all devices.
     *
     * @param batteryInfoConsumer Consumer that will receive battery events. It will be referenced using a weak reference so the owner must keep a strong reference.
     */
    public void registerUnfilteredBatteryInfoWeakListener(BiConsumer<DeviceId, BatteryInfo> batteryInfoConsumer) {
        deviceDataFrontend.registerUnfilteredBatteryInfoWeakListener(batteryInfoConsumer);
    }

    /**
     * Unregister a weak referenced listener that will receive battery info from the all devices.
     *
     * @param batteryInfoConsumer Consumer that will receive battery events. It will be referenced using a weak reference so the owner must keep a strong reference.
     */
    public void unregisterUnfilteredBatteryInfoWeakListener(BiConsumer<DeviceId, BatteryInfo> batteryInfoConsumer) {
        deviceDataFrontend.unregisterUnfilteredBatteryInfoWeakListener(batteryInfoConsumer);
    }

    /**
     * Register a weak referenced listener that will receive shifting info from the main ride device.
     *
     * @param shiftingInfoConsumer Consumer that will receive shifting events. It will be referenced using a weak reference so the owner must keep a strong reference.
     */
    public void registerShiftingInfoWeakListener(BiConsumer<DeviceId, ShiftingInfo> shiftingInfoConsumer) {
        deviceDataFrontend.registerShiftingInfoWeakListener(shiftingInfoConsumer);
    }

    /**
     * Unregister a weak referenced listener that will receive shifting info from the main ride device.
     *
     * @param shiftingInfoConsumer Consumer that will receive shifting events. It will be referenced using a weak reference so the owner must keep a strong reference.
     */
    public void unregisterShiftingInfoWeakListener(BiConsumer<DeviceId, ShiftingInfo> shiftingInfoConsumer) {
        deviceDataFrontend.unregisterShiftingInfoWeakListener(shiftingInfoConsumer);
    }

    /**
     * Register a weak referenced listener that will receive shifting info from the all devices.
     *
     * @param shiftingInfoConsumer Consumer that will receive shifting events. It will be referenced using a weak reference so the owner must keep a strong reference.
     */
    public void registerUnfilteredShiftingInfoWeakListener(BiConsumer<DeviceId, ShiftingInfo> shiftingInfoConsumer) {
        deviceDataFrontend.registerUnfilteredShiftingInfoWeakListener(shiftingInfoConsumer);
    }

    /**
     * Unregister a weak referenced listener that will receive shifting info from the all devices.
     *
     * @param shiftingInfoConsumer Consumer that will receive shifting events. It will be referenced using a weak reference so the owner must keep a strong reference.
     */
    public void unregisterUnfilteredShiftingInfoWeakListener(BiConsumer<DeviceId, ShiftingInfo> shiftingInfoConsumer) {
        deviceDataFrontend.unregisterUnfilteredShiftingInfoWeakListener(shiftingInfoConsumer);
    }

    /**
     * Register a weak referenced listener that will receive preferences info from the main ride device.
     *
     * @param devicePreferencesConsumer Consumer that will receive preferences events. It will be referenced using a weak reference so the owner must keep a strong reference.
     */
    public void registerDevicePreferencesWeakListener(BiConsumer<DeviceId, DevicePreferencesView> devicePreferencesConsumer) {
        deviceDataFrontend.registerDevicePreferencesWeakListener(devicePreferencesConsumer);
    }

    /**
     * Unregister a weak referenced listener that will receive preferences info from the main ride device.
     *
     * @param devicePreferencesConsumer Consumer that will receive preferences events. It will be referenced using a weak reference so the owner must keep a strong reference.
     */
    public void unregisterDevicePreferencesWeakListener(BiConsumer<DeviceId, DevicePreferencesView> devicePreferencesConsumer) {
        deviceDataFrontend.unregisterDevicePreferencesWeakListener(devicePreferencesConsumer);
    }

    /**
     * Register a weak referenced listener that will receive preferences info from all devices.
     *
     * @param devicePreferencesConsumer Consumer that will receive preferences events. It will be referenced using a weak reference so the owner must keep a strong reference.
     */
    public void registerUnfilteredDevicePreferencesWeakListener(BiConsumer<DeviceId, DevicePreferencesView> devicePreferencesConsumer) {
        deviceDataFrontend.registerUnfilteredDevicePreferencesWeakListener(devicePreferencesConsumer);
    }

    /**
     * Unregister a weak referenced listener that will receive preferences info from all devices.
     *
     * @param devicePreferencesConsumer Consumer that will receive preferences events. It will be referenced using a weak reference so the owner must keep a strong reference.
     */
    public void unregisterUnfilteredDevicePreferencesWeakListener(BiConsumer<DeviceId, DevicePreferencesView> devicePreferencesConsumer) {
        deviceDataFrontend.unregisterUnfilteredDevicePreferencesWeakListener(devicePreferencesConsumer);
    }

    /**
     * Get device preferences.
     *
     * @param deviceId Device identifier.
     * @return Device preference for the specified device. Can be <code>null</code> if the service is not reachable.
     */
    @Nullable
    public DevicePreferencesView getDevicePreferences(DeviceId deviceId) {
        return deviceDataFrontend.getDevicePreferences(deviceId);
    }

    /**
     * Change shift mode. The shift mode will not be changed if the service cannot be reached.
     *
     * @param deviceId Device identifier.
     */
    public void changeShiftMode(DeviceId deviceId) {
        deviceDataFrontend.changeShiftMode(deviceId);
    }

    /**
     * Register a weak referenced listener that will receive messages.
     *
     * @param messageConsumer Consumer that will receive messages. It will be referenced using a weak reference so the owner must keep a strong reference.
     */
    public void registerMessageWeakListener(Consumer<Message> messageConsumer) {
        handler.post(() -> {
            messageListeners.addListener(messageConsumer);
            maybeStartMessageEvents();
        });
    }

    /**
     * Send message. The message will not be sent if the service cannot be reached.
     *
     * @param message Message to send. Cannot be null.
     */
    public void sendMessage(@NonNull Message message) {
        if (service == null) {
            return;
        }

        try {
            service.sendMessage(message);
        } catch (RemoteException e) {
            Log.e("KI2", "Unable to send message", e);
        }
    }

    /**
     * Get custom message client.
     *
     * @return Custom message client.
     */
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

        registrationMessage.register();
    }

    private void maybeStopMessageEvents() {
        if (service == null) {
            return;
        }

        if (messageListeners.hasListeners()) {
            return;
        }

        registrationMessage.unregister();
    }

    /**
     * Get global preferences.
     *
     * @return Global preferences. Can be <code>null</code> if the service is not reachable.
     */
    @Nullable
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

    /**
     * Get saved devices.
     *
     * @return Saved devices. Can be <code>null</code> if the service is not reachable.
     */
    @Nullable
    public List<DeviceId> getSavedDevices() {
        if (service == null) {
            return null;
        }

        try {
            return service.getSavedDevices();
        } catch (Exception e) {
            Log.e("KI2", "Unable to get saved devices", e);
        }

        return null;
    }

    /**
     * Register a weak referenced listener that will receive global preferences.
     *
     * @param preferencesConsumer Consumer that will receive global preferences. It will be referenced using a weak reference so the owner must keep a strong reference.
     */
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

        registrationPreferences.register();
    }

    private void maybeStopPreferencesEvents() {
        if (service == null) {
            return;
        }

        if (preferencesListeners.hasListeners()) {
            return;
        }

        registrationPreferences.unregister();
    }

    public void startDeviceScan(Consumer<DeviceId> deviceIdConsumer) {
        handler.post(() -> {
            scanListeners.addListener(deviceIdConsumer);
            maybeStartScanEvents();
        });
    }

    public void stopDeviceScan(Consumer<DeviceId> deviceIdConsumer) {
        handler.post(() -> {
            scanListeners.removeListener(deviceIdConsumer);
            maybeStopScanEvents();
        });
    }

    private void maybeStartScanEvents() {
        if (service == null) {
            return;
        }

        if (!scanListeners.hasListeners()) {
            return;
        }

        registrationScan.register();
    }

    private void maybeStopScanEvents() {
        if (service == null) {
            return;
        }

        if (scanListeners.hasListeners()) {
            return;
        }

        registrationScan.unregister();
    }
}
