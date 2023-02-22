package com.valterc.ki2.karoo.service.device;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;

import com.valterc.ki2.data.connection.ConnectionInfo;
import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.input.KarooKeyEvent;
import com.valterc.ki2.data.preferences.device.DevicePreferencesView;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.input.InputAdapter;
import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.karoo.hooks.RideActivityHook;
import com.valterc.ki2.karoo.service.listeners.ServiceCallbackRegistration;
import com.valterc.ki2.services.IKi2Service;
import com.valterc.ki2.services.callbacks.IBatteryCallback;
import com.valterc.ki2.services.callbacks.IConnectionInfoCallback;
import com.valterc.ki2.services.callbacks.IDevicePreferencesCallback;
import com.valterc.ki2.services.callbacks.IKeyCallback;
import com.valterc.ki2.services.callbacks.IShiftingCallback;

import java.util.function.BiConsumer;

@SuppressLint("LogNotTimber")
public class DeviceDataFrontend {

    private IKi2Service service;
    private final Handler handler;
    private final InputAdapter inputAdapter;
    private final DeviceDataRouter dataRouter;

    private final ServiceCallbackRegistration<IConnectionInfoCallback> registrationConnectionInfo = new ServiceCallbackRegistration<>(new IConnectionInfoCallback.Stub() {
        @Override
        public void onConnectionInfo(DeviceId deviceId, ConnectionInfo connectionInfo) {
            handler.post(() -> {
                dataRouter.onConnectionInfo(deviceId, connectionInfo);
                maybeStopConnectionEvents();
            });
        }
    }, callback -> service.registerConnectionInfoListener(callback), callback -> service.unregisterConnectionInfoListener(callback));

    private final ServiceCallbackRegistration<IBatteryCallback> registrationBatteryInfo = new ServiceCallbackRegistration<>(new IBatteryCallback.Stub() {
        @Override
        public void onBattery(DeviceId deviceId, BatteryInfo batteryInfo) {
            handler.post(() -> {
                dataRouter.onBattery(deviceId, batteryInfo);
                maybeStopBatteryEvents();
            });
        }
    }, callback -> service.registerBatteryListener(callback), callback -> service.unregisterBatteryListener(callback));

    private final ServiceCallbackRegistration<IShiftingCallback> registrationShiftingInfo = new ServiceCallbackRegistration<>(new IShiftingCallback.Stub() {
        @Override
        public void onShifting(DeviceId deviceId, ShiftingInfo shiftingInfo) {
            handler.post(() -> {
                dataRouter.onShifting(deviceId, shiftingInfo);
                maybeStopShiftingEvents();
            });
        }
    }, callback -> service.registerShiftingListener(callback), callback -> service.unregisterShiftingListener(callback));

    private final ServiceCallbackRegistration<IKeyCallback> registrationKey = new ServiceCallbackRegistration<>(new IKeyCallback.Stub() {
        @Override
        public void onKeyEvent(DeviceId deviceId, KarooKeyEvent keyEvent) {
            handler.post(() -> {
                try {
                    inputAdapter.executeKeyEvent(keyEvent);
                } catch (Exception e) {
                    Log.e("KI2", "Error handling input", e);
                }
                maybeStopKeyEvents();
            });
        }
    }, callback -> service.registerKeyListener(callback), callback -> service.unregisterKeyListener(callback));

    private final ServiceCallbackRegistration<IDevicePreferencesCallback> registrationDevicePreferences = new ServiceCallbackRegistration<>(new IDevicePreferencesCallback.Stub() {
        @Override
        public void onDevicePreferences(DeviceId deviceId, DevicePreferencesView preferences) {
            handler.post(() -> {
                dataRouter.onDevicePreferences(deviceId, preferences);
                maybeStopDevicePreferencesEvents();
            });
        }
    }, callback -> service.registerDevicePreferencesListener(callback), callback -> service.unregisterDevicePreferencesListener(callback));

    public DeviceDataFrontend(Ki2Context ki2Context) {
        this.handler = ki2Context.getHandler();

        inputAdapter = new InputAdapter(ki2Context);
        dataRouter = new DeviceDataRouter(ki2Context.getSdkContext());
    }

    public void setService(IKi2Service service) {
        this.service = service;

        registrationConnectionInfo.setUnregistered();
        registrationBatteryInfo.setUnregistered();
        registrationShiftingInfo.setUnregistered();
        registrationKey.setUnregistered();
        registrationDevicePreferences.setUnregistered();

        if (service != null) {
            handler.post(this::maybeStartEvents);
        }
    }

    private void maybeStartEvents() {
        maybeStartConnectionEvents();
        maybeStartBatteryEvents();
        maybeStartKeyEvents();
        maybeStartShiftingEvents();
        maybeStartDevicePreferencesEvents();
    }

    public void registerConnectionInfoWeakListener(BiConsumer<DeviceId, ConnectionInfo> connectionInfoConsumer) {
        handler.post(() -> {
            dataRouter.registerConnectionInfoWeakListener(connectionInfoConsumer);
            maybeStartEvents();
        });
    }

    private void maybeStartConnectionEvents() {
        if (service == null) {
            return;
        }

        if (!dataRouter.hasConnectionInfoListeners() &&
                !dataRouter.hasShiftingInfoListeners() &&
                !dataRouter.hasBatteryInfoListeners() &&
                !dataRouter.hasDevicePreferencesListeners()) {
            return;
        }

        registrationConnectionInfo.register();
    }

    private void maybeStopConnectionEvents() {
        if (service == null) {
            return;
        }

        if (dataRouter.hasConnectionInfoListeners() ||
                dataRouter.hasShiftingInfoListeners() ||
                dataRouter.hasBatteryInfoListeners() ||
                dataRouter.hasDevicePreferencesListeners()) {
            return;
        }

        registrationConnectionInfo.unregister();
    }

    public void registerBatteryInfoWeakListener(BiConsumer<DeviceId, BatteryInfo> batteryInfoConsumer) {
        handler.post(() -> {
            dataRouter.registerBatteryInfoWeakListener(batteryInfoConsumer);
            maybeStartEvents();
        });
    }

    public void registerUnfilteredBatteryInfoWeakListener(BiConsumer<DeviceId, BatteryInfo> batteryInfoConsumer) {
        handler.post(() -> {
            dataRouter.registerUnfilteredBatteryInfoWeakListener(batteryInfoConsumer);
            maybeStartEvents();
        });
    }

    private void maybeStartBatteryEvents() {
        if (service == null) {
            return;
        }

        if (!dataRouter.hasBatteryInfoListeners()) {
            return;
        }

        registrationBatteryInfo.register();
    }

    private void maybeStopBatteryEvents() {
        if (service == null) {
            return;
        }

        if (dataRouter.hasBatteryInfoListeners()) {
            return;
        }

        registrationBatteryInfo.unregister();
    }

    public void registerShiftingInfoWeakListener(BiConsumer<DeviceId, ShiftingInfo> shiftingInfoConsumer) {
        handler.post(() -> {
            dataRouter.registerShiftingInfoWeakListener(shiftingInfoConsumer);
            maybeStartEvents();
        });
    }

    private void maybeStartShiftingEvents() {
        if (service == null) {
            return;
        }

        if (!dataRouter.hasShiftingInfoListeners()) {
            return;
        }

        registrationShiftingInfo.register();
    }

    private void maybeStopShiftingEvents() {
        if (service == null) {
            return;
        }

        if (dataRouter.hasShiftingInfoListeners()) {
            return;
        }

        registrationShiftingInfo.unregister();
    }

    public void registerDevicePreferencesWeakListener(BiConsumer<DeviceId, DevicePreferencesView> devicePreferencesConsumer) {
        handler.post(() -> {
            dataRouter.registerDevicePreferencesWeakListener(devicePreferencesConsumer);
            maybeStartEvents();
        });
    }

    private void maybeStartDevicePreferencesEvents() {
        if (service == null) {
            return;
        }

        if (!dataRouter.hasConnectionInfoListeners() &&
                !dataRouter.hasShiftingInfoListeners() &&
                !dataRouter.hasBatteryInfoListeners() &&
                !dataRouter.hasDevicePreferencesListeners()) {
            return;
        }

        registrationDevicePreferences.register();
    }

    private void maybeStopDevicePreferencesEvents() {
        if (service == null) {
            return;
        }

        if (dataRouter.hasConnectionInfoListeners() ||
                dataRouter.hasShiftingInfoListeners() ||
                dataRouter.hasBatteryInfoListeners() ||
                dataRouter.hasDevicePreferencesListeners()) {
            return;
        }

        registrationDevicePreferences.unregister();
    }

    private void maybeStartKeyEvents() {
        if (service == null) {
            return;
        }

        if (!RideActivityHook.isRideActivityProcess()) {
            return;
        }

        registrationKey.register();
    }

    private void maybeStopKeyEvents() {
        if (service == null) {
            return;
        }

        if (RideActivityHook.isRideActivityProcess()) {
            return;
        }

        registrationKey.unregister();
    }

    public DevicePreferencesView getDevicePreferences(DeviceId deviceId) {
        if (service == null) {
            return null;
        }

        try {
            return service.getDevicePreferences(deviceId);
        } catch (Exception e) {
            Log.e("KI2", "Unable to get device preferences", e);
        }

        return null;
    }

    public void changeShiftMode(DeviceId deviceId) {
        if (service == null) {
            return;
        }

        try {
            service.changeShiftMode(deviceId);
        } catch (RemoteException e) {
            Log.e("KI2", "Unable to change shift mode", e);
        }
    }

}
