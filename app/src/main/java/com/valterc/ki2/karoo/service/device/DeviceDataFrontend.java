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
import com.valterc.ki2.karoo.hooks.RideActivityHook;
import com.valterc.ki2.services.IKi2Service;
import com.valterc.ki2.services.callbacks.IBatteryCallback;
import com.valterc.ki2.services.callbacks.IConnectionInfoCallback;
import com.valterc.ki2.services.callbacks.IDevicePreferencesCallback;
import com.valterc.ki2.services.callbacks.IKeyCallback;
import com.valterc.ki2.services.callbacks.IShiftingCallback;

import java.util.function.BiConsumer;

import io.hammerhead.sdk.v0.SdkContext;

@SuppressLint("LogNotTimber")
public class DeviceDataFrontend {

    private final IConnectionInfoCallback connectionInfoCallback = new IConnectionInfoCallback.Stub() {
        @Override
        public void onConnectionInfo(DeviceId deviceId, ConnectionInfo connectionInfo) {
            handler.post(() -> {
                dataRouter.onConnectionInfo(deviceId, connectionInfo);
                maybeStopConnectionEvents();
            });
        }
    };

    private final IBatteryCallback batteryCallback = new IBatteryCallback.Stub() {
        @Override
        public void onBattery(DeviceId deviceId, BatteryInfo batteryInfo) {
            handler.post(() -> {
                dataRouter.onBattery(deviceId, batteryInfo);
                maybeStopBatteryEvents();
            });
        }
    };

    private final IShiftingCallback shiftingCallback = new IShiftingCallback.Stub() {
        @Override
        public void onShifting(DeviceId deviceId, ShiftingInfo shiftingInfo) {
            handler.post(() -> {
                dataRouter.onShifting(deviceId, shiftingInfo);
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
                    Log.e("KI2", "Error handling input", e);
                }
                maybeStopKeyEvents();
            });
        }
    };

    private final IDevicePreferencesCallback devicePreferencesCallback = new IDevicePreferencesCallback.Stub() {
        @Override
        public void onDevicePreferences(DeviceId deviceId, DevicePreferencesView preferences) {
            handler.post(() -> {
                dataRouter.onDevicePreferences(deviceId, preferences);
                maybeStopDevicePreferencesEvents();
            });
        }
    };

    private IKi2Service service;
    private final Handler handler;
    private final InputAdapter inputAdapter;
    private final DeviceDataRouter dataRouter;

    public DeviceDataFrontend(SdkContext context, Handler handler) {
        this.handler = handler;

        inputAdapter = new InputAdapter(context);
        dataRouter = new DeviceDataRouter(context);
    }

    public void setService(IKi2Service service) {
        this.service = service;

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

        if (dataRouter.hasConnectionInfoListeners() ||
                dataRouter.hasShiftingInfoListeners() ||
                dataRouter.hasBatteryInfoListeners() ||
                dataRouter.hasDevicePreferencesListeners()) {
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

        if (dataRouter.hasBatteryInfoListeners()) {
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

        if (dataRouter.hasShiftingInfoListeners()) {
            return;
        }

        try {
            service.unregisterShiftingListener(shiftingCallback);
        } catch (Exception e) {
            Log.e("KI2", "Unable to unregister listener", e);
        }
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

        try {
            service.registerDevicePreferencesListener(devicePreferencesCallback);
        } catch (RemoteException e) {
            Log.e("KI2", "Unable to register device preferences listener", e);
        }
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

        try {
            service.unregisterDevicePreferencesListener(devicePreferencesCallback);
        } catch (RemoteException e) {
            Log.e("KI2", "Unable to unregister device preferences listener", e);
        }
    }

    private void maybeStartKeyEvents() {
        if (service == null) {
            return;
        }

        if (!RideActivityHook.isRideActivityProcess()) {
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

        if (RideActivityHook.isRideActivityProcess()) {
            return;
        }

        try {
            service.unregisterKeyListener(keyCallback);
        } catch (RemoteException e) {
            Log.e("KI2", "Unable to unregister listener", e);
        }
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
