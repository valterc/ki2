package com.valterc.ki2.karoo;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.valterc.ki2.data.connection.ConnectionInfo;
import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.data.switches.SwitchKeyEvent;
import com.valterc.ki2.input.InputAdapter;
import com.valterc.ki2.services.IKi2Service;
import com.valterc.ki2.services.Ki2Service;
import com.valterc.ki2.services.callbacks.IBatteryCallback;
import com.valterc.ki2.services.callbacks.IConnectionInfoCallback;
import com.valterc.ki2.services.callbacks.IShiftingCallback;
import com.valterc.ki2.services.callbacks.ISwitchKeyCallback;

import java.util.WeakHashMap;
import java.util.function.Consumer;

import io.hammerhead.sdk.v0.SdkContext;

@SuppressLint("LogNotTimber")
public class Ki2ServiceClient {

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = IKi2Service.Stub.asInterface(binder);
            handler.post(() -> {
                maybeStartSwitchKeyEvents();
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
                connectionInfoListeners.keySet().forEach(c -> {
                    try {
                        c.accept(connectionInfo);
                    } catch (Exception e) {
                        Log.e("KI2", "Error during callback", e);
                    }
                });
                maybeStopConnectionEvents();
            });
        }
    };

    private final IBatteryCallback batteryCallback = new IBatteryCallback.Stub() {
        @Override
        public void onBattery(DeviceId deviceId, BatteryInfo batteryInfo) {
            handler.post(() -> {
                batteryInfoListeners.keySet().forEach(c -> {
                    try {
                        c.accept(batteryInfo);
                    } catch (Exception e) {
                        Log.e("KI2", "Error during callback", e);
                    }
                });
                maybeStopBatteryEvents();
            });
        }
    };

    private final IShiftingCallback shiftingCallback = new IShiftingCallback.Stub() {
        @Override
        public void onShifting(DeviceId deviceId, ShiftingInfo shiftingInfo) {
            handler.post(() -> {
                shiftingInfoListeners.keySet().forEach(c -> {
                    try {
                        c.accept(shiftingInfo);
                    } catch (Exception e) {
                        Log.e("KI2", "Error during callback", e);
                    }
                });
                maybeStopShiftingEvents();
            });
        }
    };

    private final ISwitchKeyCallback switchKeyCallback = new ISwitchKeyCallback.Stub() {
        @Override
        public void onSwitchKeyEvent(DeviceId deviceId, SwitchKeyEvent switchKeyEvent) {
            handler.post(() -> {
                try {
                    inputAdapter.executeKeyEvent(switchKeyEvent);
                } catch (Exception e) {
                    Log.e("KI2", "Error during callback", e);
                }
                maybeStopSwitchKeyEvents();
            });
        }
    };

    private final InputAdapter inputAdapter;
    private final Handler handler;
    private final WeakHashMap<Consumer<ConnectionInfo>, Boolean> connectionInfoListeners;
    private final WeakHashMap<Consumer<BatteryInfo>, Boolean> batteryInfoListeners;
    private final WeakHashMap<Consumer<ShiftingInfo>, Boolean> shiftingInfoListeners;
    private IKi2Service service;

    public Ki2ServiceClient(SdkContext context) {
        inputAdapter = new InputAdapter(context);
        connectionInfoListeners = new WeakHashMap<>();
        batteryInfoListeners = new WeakHashMap<>();
        shiftingInfoListeners = new WeakHashMap<>();
        handler = new Handler(Looper.getMainLooper());
        context.bindService(Ki2Service.getIntent(), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void registerConnectionInfoListener(Consumer<ConnectionInfo> connectionInfoConsumer) {
        handler.post(() -> {
            connectionInfoListeners.put(connectionInfoConsumer, null);
            maybeStartConnectionEvents();
            maybeStartSwitchKeyEvents();
        });
    }

    private void maybeStartConnectionEvents() {
        if (service == null) {
            return;
        }

        if (connectionInfoListeners.size() == 0) {
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

        if (connectionInfoListeners.size() != 0) {
            return;
        }

        try {
            service.unregisterConnectionInfoListener(connectionInfoCallback);
        } catch (Exception e) {
            Log.e("KI2", "Unable to unregister listener", e);
        }
    }

    public void registerBatteryInfoListener(Consumer<BatteryInfo> batteryInfoConsumer) {
        handler.post(() -> {
            batteryInfoListeners.put(batteryInfoConsumer, null);
            maybeStartBatteryEvents();
            maybeStartSwitchKeyEvents();
        });
    }

    private void maybeStartBatteryEvents() {
        if (service == null) {
            return;
        }

        if (batteryInfoListeners.size() == 0) {
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

        if (batteryInfoListeners.size() != 0) {
            return;
        }

        try {
            service.unregisterBatteryListener(batteryCallback);
        } catch (Exception e) {
            Log.e("KI2", "Unable to unregister listener", e);
        }
    }

    public void registerShiftingInfoListener(Consumer<ShiftingInfo> shiftingInfoConsumer) {
        handler.post(() -> {
            shiftingInfoListeners.put(shiftingInfoConsumer, null);
            maybeStartShiftingEvents();
        });
    }

    private void maybeStartShiftingEvents() {
        if (service == null) {
            return;
        }

        if (shiftingInfoListeners.size() == 0) {
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

        if (shiftingInfoListeners.size() != 0) {
            return;
        }

        try {
            service.unregisterShiftingListener(shiftingCallback);
        } catch (Exception e) {
            Log.e("KI2", "Unable to unregister listener", e);
        }
    }

    private void maybeStartSwitchKeyEvents() {
        if (service == null) {
            return;
        }

        if (shiftingInfoListeners.size() == 0 &&
                connectionInfoListeners.size() == 0 &&
                batteryInfoListeners.size() == 0) {
            return;
        }

        try {
            service.registerSwitchKeyListener(switchKeyCallback);
        } catch (RemoteException e) {
            Log.e("KI2", "Unable to register listener", e);
        }
    }

    private void maybeStopSwitchKeyEvents() {
        if (service == null) {
            return;
        }

        if (shiftingInfoListeners.size() != 0 ||
                connectionInfoListeners.size() != 0 ||
                batteryInfoListeners.size() != 0) {
            return;
        }

        try {
            service.unregisterSwitchKeyListener(switchKeyCallback);
        } catch (RemoteException e) {
            Log.e("KI2", "Unable to unregister listener", e);
        }
    }

}
