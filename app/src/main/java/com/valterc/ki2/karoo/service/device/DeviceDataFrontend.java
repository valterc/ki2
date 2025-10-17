package com.valterc.ki2.karoo.service.device;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;

import androidx.annotation.Nullable;

import com.valterc.ki2.data.action.KarooActionEvent;
import com.valterc.ki2.data.connection.ConnectionInfo;
import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.preferences.device.DevicePreferencesView;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.karoo.service.listeners.ServiceCallbackRegistration;
import com.valterc.ki2.services.IKi2Service;
import com.valterc.ki2.services.callbacks.IActionCallback;
import com.valterc.ki2.services.callbacks.IBatteryCallback;
import com.valterc.ki2.services.callbacks.IConnectionInfoCallback;
import com.valterc.ki2.services.callbacks.IDevicePreferencesCallback;
import com.valterc.ki2.services.callbacks.IShiftingCallback;

import java.util.function.BiConsumer;

import timber.log.Timber;

public class DeviceDataFrontend {

    private IKi2Service service;
    private final Handler handler;
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

    private final ServiceCallbackRegistration<IActionCallback> registrationAction = new ServiceCallbackRegistration<>(new IActionCallback.Stub() {
        @Override
        public void onActionEvent(DeviceId deviceId, KarooActionEvent actionEvent) {
            handler.post(() -> {
                dataRouter.onActionEvent(deviceId, actionEvent);
                maybeStopActionEvents();
            });
        }
    }, callback -> service.registerActionListener(callback), callback -> service.unregisterActionListener(callback));

    private final ServiceCallbackRegistration<IDevicePreferencesCallback> registrationDevicePreferences = new ServiceCallbackRegistration<>(new IDevicePreferencesCallback.Stub() {
        @Override
        public void onDevicePreferences(DeviceId deviceId, DevicePreferencesView preferences) {
            handler.post(() -> {
                dataRouter.onDevicePreferences(deviceId, preferences);
                maybeStopDevicePreferencesEvents();
            });
        }
    }, callback -> service.registerDevicePreferencesListener(callback), callback -> service.unregisterDevicePreferencesListener(callback));

    public DeviceDataFrontend(Context context) {
        this.handler = new Handler(Looper.getMainLooper());
        dataRouter = new DeviceDataRouter(context);
    }

    public void setService(IKi2Service service) {
        Timber.i("Setting service reference to: %s", service != null ? "Ki2Service" : "null");
        this.service = service;

        registrationConnectionInfo.setUnregistered();
        registrationBatteryInfo.setUnregistered();
        registrationShiftingInfo.setUnregistered();
        registrationAction.setUnregistered();
        registrationDevicePreferences.setUnregistered();

        if (service != null) {
            handler.post(this::maybeStartEvents);
        }
    }

    private void maybeStartEvents() {
        maybeStartConnectionEvents();
        maybeStartBatteryEvents();
        maybeStartActionEvents();
        maybeStartShiftingEvents();
        maybeStartDevicePreferencesEvents();
    }

    public void registerConnectionInfoWeakListener(BiConsumer<DeviceId, ConnectionInfo> connectionInfoConsumer) {
        handler.post(() -> {
            dataRouter.registerConnectionInfoWeakListener(connectionInfoConsumer);
            maybeStartEvents();
        });
    }

    public void unregisterConnectionInfoWeakListener(BiConsumer<DeviceId, ConnectionInfo> connectionInfoConsumer) {
        handler.post(() -> {
            dataRouter.unregisterConnectionInfoWeakListener(connectionInfoConsumer);
            maybeStopConnectionEvents();
        });
    }

    public void registerUnfilteredConnectionInfoWeakListener(BiConsumer<DeviceId, ConnectionInfo> connectionInfoConsumer) {
        handler.post(() -> {
            dataRouter.registerUnfilteredConnectionInfoWeakListener(connectionInfoConsumer);
            maybeStartEvents();
        });
    }

    public void unregisterUnfilteredConnectionInfoWeakListener(BiConsumer<DeviceId, ConnectionInfo> connectionInfoConsumer) {
        handler.post(() -> {
            dataRouter.unregisterUnfilteredConnectionInfoWeakListener(connectionInfoConsumer);
            maybeStopConnectionEvents();
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

        Timber.i("Registering for connection events");
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

    public void unregisterBatteryInfoWeakListener(BiConsumer<DeviceId, BatteryInfo> batteryInfoConsumer) {
        handler.post(() -> {
            dataRouter.unregisterBatteryInfoWeakListener(batteryInfoConsumer);
            maybeStopBatteryEvents();
        });
    }

    public void registerUnfilteredBatteryInfoWeakListener(BiConsumer<DeviceId, BatteryInfo> batteryInfoConsumer) {
        handler.post(() -> {
            dataRouter.registerUnfilteredBatteryInfoWeakListener(batteryInfoConsumer);
            maybeStartEvents();
        });
    }

    public void unregisterUnfilteredBatteryInfoWeakListener(BiConsumer<DeviceId, BatteryInfo> batteryInfoConsumer) {
        handler.post(() -> {
            dataRouter.unregisterUnfilteredBatteryInfoWeakListener(batteryInfoConsumer);
            maybeStopBatteryEvents();
        });
    }

    private void maybeStartBatteryEvents() {
        if (service == null) {
            return;
        }

        if (!dataRouter.hasBatteryInfoListeners()) {
            return;
        }

        Timber.i("Registering for battery events");
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

    public void unregisterShiftingInfoWeakListener(BiConsumer<DeviceId, ShiftingInfo> shiftingInfoConsumer) {
        handler.post(() -> {
            dataRouter.unregisterShiftingInfoWeakListener(shiftingInfoConsumer);
            maybeStopShiftingEvents();
        });
    }

    public void registerUnfilteredShiftingInfoWeakListener(BiConsumer<DeviceId, ShiftingInfo> shiftingInfoConsumer) {
        handler.post(() -> {
            dataRouter.registerUnfilteredShiftingInfoWeakListener(shiftingInfoConsumer);
            maybeStartEvents();
        });
    }

    public void unregisterUnfilteredShiftingInfoWeakListener(BiConsumer<DeviceId, ShiftingInfo> shiftingInfoConsumer) {
        handler.post(() -> {
            dataRouter.unregisterUnfilteredShiftingInfoWeakListener(shiftingInfoConsumer);
            maybeStopShiftingEvents();
        });
    }

    private void maybeStartShiftingEvents() {
        if (service == null) {
            return;
        }

        if (!dataRouter.hasShiftingInfoListeners()) {
            return;
        }

        Timber.i("Registering for shifting events");
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

    public void unregisterDevicePreferencesWeakListener(BiConsumer<DeviceId, DevicePreferencesView> devicePreferencesConsumer) {
        handler.post(() -> {
            dataRouter.unregisterDevicePreferencesWeakListener(devicePreferencesConsumer);
            maybeStopDevicePreferencesEvents();
        });
    }

    public void registerUnfilteredDevicePreferencesWeakListener(BiConsumer<DeviceId, DevicePreferencesView> devicePreferencesConsumer) {
        handler.post(() -> {
            dataRouter.registerUnfilteredDevicePreferencesWeakListener(devicePreferencesConsumer);
            maybeStartEvents();
        });
    }

    public void unregisterUnfilteredDevicePreferencesWeakListener(BiConsumer<DeviceId, DevicePreferencesView> devicePreferencesConsumer) {
        handler.post(() -> {
            dataRouter.unregisterUnfilteredDevicePreferencesWeakListener(devicePreferencesConsumer);
            maybeStopDevicePreferencesEvents();
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

    public void registerActionEventWeakListener(BiConsumer<DeviceId, KarooActionEvent> actionEventConsumer) {
        handler.post(() -> {
            dataRouter.registerActionEventListener(actionEventConsumer);
            maybeStartEvents();
        });
    }

    public void unregisterActionEventWeakListener(BiConsumer<DeviceId, KarooActionEvent> actionEventConsumer) {
        handler.post(() -> {
            dataRouter.unregisterActionEventListener(actionEventConsumer);
            maybeStopActionEvents();
        });
    }

    private void maybeStartActionEvents() {
        if (service == null) {
            return;
        }

        if (!dataRouter.hasKeyListeners()) {
            return;
        }

        registrationAction.register();
    }

    private void maybeStopActionEvents() {
        if (service == null) {
            return;
        }

        if (dataRouter.hasKeyListeners()) {
            return;
        }

        registrationAction.unregister();
    }

    @Nullable
    public DevicePreferencesView getDevicePreferences(DeviceId deviceId) {
        if (service == null) {
            return null;
        }

        try {
            return service.getDevicePreferences(deviceId);
        } catch (Exception e) {
            Timber.e(e, "Unable to get device preferences");
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
            Timber.e(e, "Unable to change shift mode");
        }
    }

}
