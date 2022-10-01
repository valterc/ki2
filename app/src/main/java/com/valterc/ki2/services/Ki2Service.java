package com.valterc.ki2.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcelable;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.valterc.ki2.ant.AntManager;
import com.valterc.ki2.ant.IAntStateListener;
import com.valterc.ki2.ant.connection.AntConnectionManager;
import com.valterc.ki2.ant.connection.IAntDeviceConnection;
import com.valterc.ki2.ant.connection.IDeviceConnectionListener;
import com.valterc.ki2.ant.scanner.AntScanner;
import com.valterc.ki2.ant.scanner.IAntScanListener;
import com.valterc.ki2.data.command.CommandType;
import com.valterc.ki2.data.configuration.ConfigurationStore;
import com.valterc.ki2.data.connection.ConnectionDataManager;
import com.valterc.ki2.data.connection.ConnectionStatus;
import com.valterc.ki2.data.connection.ConnectionsDataManager;
import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.device.DeviceStore;
import com.valterc.ki2.data.info.DataType;
import com.valterc.ki2.data.info.ManufacturerInfo;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.data.switches.SwitchEvent;
import com.valterc.ki2.data.switches.SwitchKeyEvent;
import com.valterc.ki2.input.InputManager;
import com.valterc.ki2.services.callbacks.IBatteryCallback;
import com.valterc.ki2.services.callbacks.IConnectionDataInfoCallback;
import com.valterc.ki2.services.callbacks.IConnectionInfoCallback;
import com.valterc.ki2.services.callbacks.IManufacturerInfoCallback;
import com.valterc.ki2.services.callbacks.IScanCallback;
import com.valterc.ki2.services.callbacks.IShiftingCallback;
import com.valterc.ki2.services.callbacks.ISwitchCallback;
import com.valterc.ki2.services.callbacks.ISwitchKeyCallback;
import com.valterc.ki2.services.handler.ServiceHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import timber.log.Timber;

public class Ki2Service extends Service implements IAntStateListener, IAntScanListener, IDeviceConnectionListener {

    private final RemoteCallbackList<IConnectionDataInfoCallback> callbackListConnectionDataInfo
            = new RemoteCallbackList<>();
    private final RemoteCallbackList<IConnectionInfoCallback> callbackListConnectionInfo
            = new RemoteCallbackList<>();
    private final RemoteCallbackList<IManufacturerInfoCallback> callbackListManufacturerInfo
            = new RemoteCallbackList<>();
    private final RemoteCallbackList<IBatteryCallback> callbackListBattery
            = new RemoteCallbackList<>();
    private final RemoteCallbackList<IShiftingCallback> callbackListShifting
            = new RemoteCallbackList<>();
    private final RemoteCallbackList<ISwitchCallback> callbackListSwitch
            = new RemoteCallbackList<>();
    private final RemoteCallbackList<ISwitchKeyCallback> callbackListSwitchKey
            = new RemoteCallbackList<>();
    private final RemoteCallbackList<IScanCallback> callbackListScan
            = new RemoteCallbackList<>();

    private final IKi2Service.Stub binder = new IKi2Service.Stub() {
        @Override
        public void registerConnectionDataInfoListener(IConnectionDataInfoCallback callback) {
            if (callback != null) {
                callbackListConnectionDataInfo.register(callback);
            }

            serviceHandler.postAction(() -> {
                for (ConnectionDataManager connectionDataManager : connectionsDataManager.getDataManagers()) {
                    try {
                        assert callback != null;
                        callback.onConnectionDataInfo(
                                connectionDataManager.getDeviceId(),
                                connectionDataManager.buildConnectionDataInfo());
                    } catch (RemoteException e) {
                        break;
                    }
                }
            });
            serviceHandler.postRetriableAction(Ki2Service.this::processConnections);
        }

        @Override
        public void unregisterConnectionDataInfoListener(IConnectionDataInfoCallback callback) {
            if (callback != null) {
                callbackListConnectionDataInfo.unregister(callback);
            }

            serviceHandler.postRetriableAction(Ki2Service.this::processConnections);
        }

        @Override
        public void registerConnectionInfoListener(IConnectionInfoCallback callback) {
            if (callback != null) {
                callbackListConnectionInfo.register(callback);
            }

            serviceHandler.postAction(() -> {
                for (ConnectionDataManager connectionDataManager : connectionsDataManager.getDataManagers()) {
                    try {
                        assert callback != null;
                        callback.onConnectionInfo(
                                connectionDataManager.getDeviceId(),
                                connectionDataManager.buildConnectionInfo());
                    } catch (RemoteException e) {
                        break;
                    }
                }
            });
            serviceHandler.postRetriableAction(Ki2Service.this::processConnections);
        }

        @Override
        public void unregisterConnectionInfoListener(IConnectionInfoCallback callback) {
            if (callback != null) {
                callbackListConnectionInfo.unregister(callback);
            }

            serviceHandler.postRetriableAction(Ki2Service.this::processConnections);
        }

        @Override
        public void registerShiftingListener(IShiftingCallback callback) {
            if (callback != null) {
                callbackListShifting.register(callback);
            }

            serviceHandler.postAction(() -> {
                for (ConnectionDataManager connectionDataManager : connectionsDataManager.getDataManagers()) {
                    try {
                        ShiftingInfo shiftingInfo = (ShiftingInfo) connectionDataManager.getData(DataType.SHIFTING);
                        if (shiftingInfo != null) {
                            assert callback != null;
                            callback.onShifting(
                                    connectionDataManager.getDeviceId(),
                                    shiftingInfo);
                        }
                    } catch (RemoteException e) {
                        break;
                    }
                }
            });
            serviceHandler.postRetriableAction(Ki2Service.this::processConnections);
        }

        @Override
        public void unregisterShiftingListener(IShiftingCallback callback) {
            if (callback != null) {
                callbackListShifting.unregister(callback);
            }

            serviceHandler.postRetriableAction(Ki2Service.this::processConnections);
        }

        @Override
        public void registerBatteryListener(IBatteryCallback callback) {
            if (callback != null) {
                callbackListBattery.register(callback);
            }

            serviceHandler.postAction(() -> {
                for (ConnectionDataManager connectionDataManager : connectionsDataManager.getDataManagers()) {
                    try {
                        BatteryInfo batteryInfo = (BatteryInfo) connectionDataManager.getData(DataType.BATTERY);
                        if (batteryInfo != null) {
                            assert callback != null;
                            callback.onBattery(
                                    connectionDataManager.getDeviceId(),
                                    batteryInfo);
                        }
                    } catch (RemoteException e) {
                        break;
                    }
                }
            });
            serviceHandler.postRetriableAction(Ki2Service.this::processConnections);
        }

        @Override
        public void unregisterBatteryListener(IBatteryCallback callback) {
            if (callback != null) {
                callbackListBattery.unregister(callback);
            }

            serviceHandler.postRetriableAction(Ki2Service.this::processConnections);
        }

        @Override
        public void registerManufacturerInfoListener(IManufacturerInfoCallback callback) {
            if (callback != null) {
                callbackListManufacturerInfo.register(callback);
            }

            serviceHandler.postAction(() -> {
                for (ConnectionDataManager connectionDataManager : connectionsDataManager.getDataManagers()) {
                    try {
                        ManufacturerInfo manufacturerInfo = (ManufacturerInfo) connectionDataManager.getData(DataType.MANUFACTURER_INFO);
                        if (manufacturerInfo != null) {
                            assert callback != null;
                            callback.onManufacturerInfo(
                                    connectionDataManager.getDeviceId(),
                                    manufacturerInfo);
                        }
                    } catch (RemoteException e) {
                        break;
                    }
                }
            });
            serviceHandler.postRetriableAction(Ki2Service.this::processConnections);
        }

        @Override
        public void unregisterManufacturerInfoListener(IManufacturerInfoCallback callback) {
            if (callback != null) {
                callbackListManufacturerInfo.unregister(callback);
            }

            serviceHandler.postRetriableAction(Ki2Service.this::processConnections);
        }

        @Override
        public void registerSwitchKeyListener(ISwitchKeyCallback callback) {
            if (callback != null) {
                callbackListSwitchKey.register(callback);
            }

            serviceHandler.postRetriableAction(Ki2Service.this::processConnections);
        }

        @Override
        public void unregisterSwitchKeyListener(ISwitchKeyCallback callback) {
            if (callback != null) {
                callbackListSwitchKey.unregister(callback);
            }

            serviceHandler.postRetriableAction(Ki2Service.this::processConnections);
        }

        @Override
        public void registerSwitchListener(ISwitchCallback callback) {
            if (callback != null) {
                callbackListSwitch.register(callback);
            }

            serviceHandler.postRetriableAction(Ki2Service.this::processConnections);
        }

        @Override
        public void unregisterSwitchListener(ISwitchCallback callback) {
            if (callback != null) {
                callbackListSwitch.unregister(callback);
            }

            serviceHandler.postRetriableAction(Ki2Service.this::processConnections);
        }

        @Override
        public void registerScanListener(IScanCallback callback) {
            if (callback != null) {
                callbackListScan.register(callback);
            }

            serviceHandler.postRetriableAction(Ki2Service.this::processScan);
        }

        @Override
        public void unregisterScanListener(IScanCallback callback) {
            if (callback != null) {
                callbackListScan.unregister(callback);
            }

            serviceHandler.postRetriableAction(Ki2Service.this::processScan);
        }

        @Override
        public void changeShiftMode(DeviceId deviceId) throws RemoteException {
            Ki2Service.this.changeShiftMode(deviceId);
        }

        @Override
        public void reconnectDevice(DeviceId deviceId) {
            serviceHandler.postRetriableAction(() -> {
                antConnectionManager.disconnect(deviceId);
                connectionsDataManager.removeConnection(deviceId);

                connectionsDataManager.addConnection(deviceId);
                antConnectionManager.connect(deviceId, Ki2Service.this);
            });
        }

        @Override
        public void saveDevice(DeviceId deviceId) {
            deviceStore.saveDevice(deviceId);
            serviceHandler.postRetriableAction(Ki2Service.this::processConnections);
        }

        @Override
        public void deleteDevice(DeviceId deviceId) {
            deviceStore.deleteDevice(deviceId);
            serviceHandler.postRetriableAction(Ki2Service.this::processConnections);
        }

        @Override
        public List<DeviceId> getSavedDevices() {
            return new ArrayList<>(deviceStore.getDevices());
        }
    };

    private NotificationManager notificationManager;
    private AntManager antManager;
    private AntScanner antScanner;
    private AntConnectionManager antConnectionManager;
    private ServiceHandler serviceHandler;
    private DeviceStore deviceStore;
    private ConnectionsDataManager connectionsDataManager;
    private InputManager inputManager;

    @Override
    public IBinder onBind(Intent arg0) {
        return binder;
    }

    @Override
    public void onCreate() {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        antManager = new AntManager(this, this);
        antScanner = new AntScanner(antManager, this);
        antConnectionManager = new AntConnectionManager(this, antManager);
        serviceHandler = new ServiceHandler();
        deviceStore = new DeviceStore(this);
        connectionsDataManager = new ConnectionsDataManager();
        inputManager = new InputManager(this);

        Timber.i("Service created");
    }

    @Override
    public void onDestroy() {
        antManager.dispose();
        antManager = null;

        callbackListManufacturerInfo.kill();
        callbackListBattery.kill();
        callbackListShifting.kill();
        callbackListSwitch.kill();

        super.onDestroy();
    }

    private void processScan() throws Exception {
        if (callbackListScan.getRegisteredCallbackCount() != 0) {
            if (antManager.isReady()) {
                antScanner.startScan(ConfigurationStore.getScanChannelConfiguration(Ki2Service.this));
            }
        } else {
            antScanner.stopScan();
        }
    }

    private void processConnections() throws Exception {
        if (callbackListSwitch.getRegisteredCallbackCount() != 0
                || callbackListConnectionInfo.getRegisteredCallbackCount() != 0
                || callbackListBattery.getRegisteredCallbackCount() != 0
                || callbackListConnectionDataInfo.getRegisteredCallbackCount() != 0
                || callbackListManufacturerInfo.getRegisteredCallbackCount() != 0
                || callbackListShifting.getRegisteredCallbackCount() != 0
                || callbackListSwitchKey.getRegisteredCallbackCount() != 0) {
            if (antManager.isReady()) {
                Collection<DeviceId> devices = deviceStore.getDevices();
                connectionsDataManager.setConnections(devices);
                antConnectionManager.connectOnly(devices, this);
            }
        } else {
            connectionsDataManager.clearConnections();
            antConnectionManager.disconnectAll();
        }
    }

    private void changeShiftMode(DeviceId deviceId) throws RemoteException {
        sendCommandToDevice(deviceId, CommandType.SHIFTING_MODE, null);
    }

    private void sendCommandToDevice(DeviceId deviceId, CommandType commandType, Object data) throws RemoteException {
        IAntDeviceConnection antDeviceConnection = antConnectionManager.getConnection(deviceId);

        if (antDeviceConnection == null) {
            throw new RemoteException("No connection to device " + deviceId);
        }

        if (antDeviceConnection.getConnectionStatus() != ConnectionStatus.ESTABLISHED) {
            throw new RemoteException("Connection to device " + deviceId + " is not established");
        }

        try {
            antDeviceConnection.sendCommand(commandType, data);
            Timber.i("Sent command %s to device %s", commandType, deviceId);
        } catch (Exception e) {
            Timber.e(e, "Unable to send command %s to device %s", commandType, deviceId);
            throw new RemoteException("Unable to change shift mode");
        }
    }

    @Override
    public void onAntStateChange(boolean ready) {
        serviceHandler.postAction(() -> {
            if (!ready) {
                antScanner.stopScan();
                antConnectionManager.disconnectAll();
            } else {
                serviceHandler.postRetriableAction(Ki2Service.this::processScan);
                serviceHandler.postRetriableAction(Ki2Service.this::processConnections);
            }
        });
    }

    @Override
    public void onAntScanResult(DeviceId deviceId) {
        serviceHandler.postAction(() -> broadcastScanResult(deviceId));
    }

    @Override
    public void onConnectionStatus(DeviceId deviceId, ConnectionStatus connectionStatus) {
        serviceHandler.postAction(() -> {
            boolean sendUpdate = connectionsDataManager.onConnectionStatus(deviceId, connectionStatus);

            if (sendUpdate) {
                broadcastData(callbackListConnectionInfo,
                        () -> connectionsDataManager.buildConnectionInfo(deviceId),
                        (callback, connectionInfo) -> callback.onConnectionInfo(deviceId, connectionInfo));
                broadcastData(callbackListConnectionDataInfo,
                        () -> connectionsDataManager.buildConnectionDataInfo(deviceId),
                        (callback, connectionDataInfo) -> callback.onConnectionDataInfo(deviceId, connectionDataInfo));
            }
        });
    }

    @Override
    public void onData(DeviceId deviceId, DataType dataType, Object data) {

        if (dataType == DataType.UNKNOWN || !(data instanceof Parcelable)) {
            Timber.d("[%s] Unsupported data (type=%s, value=%s)", deviceId, dataType, data);
            return;
        }

        serviceHandler.postAction(() -> {
            boolean sendUpdate = connectionsDataManager.onData(deviceId, dataType, (Parcelable) data);

            if (sendUpdate) {
                switch (dataType) {

                    case SHIFTING:
                        broadcastData(callbackListShifting,
                                () -> (ShiftingInfo) connectionsDataManager.getData(deviceId, dataType),
                                (callback, shiftingInfo) -> callback.onShifting(deviceId, shiftingInfo));
                        break;

                    case BATTERY:
                        broadcastData(callbackListBattery,
                                () -> (BatteryInfo) connectionsDataManager.getData(deviceId, dataType),
                                (callback, battery) -> callback.onBattery(deviceId, battery));
                        break;

                    case SWITCH:
                        SwitchEvent switchEvent = (SwitchEvent) connectionsDataManager.getData(deviceId, dataType);

                        if (switchEvent != null) {
                            broadcastData(callbackListSwitch,
                                    () -> switchEvent,
                                    (callback, se) -> callback.onSwitchEvent(deviceId, se));
                        }

                        SwitchKeyEvent switchKeyEvent = inputManager.onSwitch(switchEvent);
                        if (switchKeyEvent != null) {
                            broadcastData(callbackListSwitchKey,
                                    () -> switchKeyEvent,
                                    (callback, ske) -> callback.onSwitchKeyEvent(deviceId, ske));
                        }
                        break;

                    case SWITCH_KEY:
                        broadcastData(callbackListSwitchKey,
                                () -> (SwitchKeyEvent) connectionsDataManager.getData(deviceId, dataType),
                                (callback, ske) -> callback.onSwitchKeyEvent(deviceId, ske));
                        break;

                    case MANUFACTURER_INFO:
                        broadcastData(callbackListManufacturerInfo,
                                () -> (ManufacturerInfo) connectionsDataManager.getData(deviceId, dataType),
                                (callback, manufacturerInfo) -> callback.onManufacturerInfo(deviceId, manufacturerInfo));
                        break;

                    default:
                        Timber.d("[%s] Unsupported data type %s", deviceId, dataType);
                }
            }

        });
    }

    private void broadcastScanResult(DeviceId deviceId) {
        int count = callbackListScan.beginBroadcast();
        for (int i = 0; i < count; i++) {
            try {
                callbackListScan.getBroadcastItem(i).onScanResult(deviceId);
            } catch (RemoteException e) {
                // ignore
            }
        }
        callbackListScan.finishBroadcast();
    }

    private <TData,
            TCallback extends IInterface,
            TCallbackList extends RemoteCallbackList<TCallback>>
    void broadcastData(TCallbackList callbackList,
                       Supplier<TData> dataSupplier,
                       UnsafeBroadcastInvoker<TCallback, TData> broadcastConsumer) {
        int count = callbackList.getRegisteredCallbackCount();
        if (count == 0) {
            return;
        }

        TData data = dataSupplier.get();

        if (data == null) {
            return;
        }

        count = callbackList.beginBroadcast();
        for (int i = 0; i < count; i++) {
            try {
                broadcastConsumer.invoke(callbackList.getBroadcastItem(i), data);
            } catch (RemoteException e) {
                // ignore
            }
        }
        callbackList.finishBroadcast();
    }
}
