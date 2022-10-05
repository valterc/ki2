package com.valterc.ki2.fragments.devices.details;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.valterc.ki2.data.connection.ConnectionDataInfo;
import com.valterc.ki2.data.connection.ConnectionStatus;
import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.device.SignalInfo;
import com.valterc.ki2.data.info.DataInfo;
import com.valterc.ki2.data.info.DataType;
import com.valterc.ki2.data.info.ManufacturerInfo;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.data.switches.SwitchEvent;
import com.valterc.ki2.services.IKi2Service;
import com.valterc.ki2.services.callbacks.IConnectionDataInfoCallback;

import java.util.Map;

import timber.log.Timber;

public class DeviceDetailsViewModel extends ViewModel {

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Timber.d("Service connected");

            IKi2Service ki2Service = IKi2Service.Stub.asInterface(binder);
            service.postValue(ki2Service);

            try {
                ki2Service.registerConnectionDataInfoListener(connectionDataInfoCallback);
            } catch (RemoteException e) {
                Timber.e("Unable to register connection data info listener");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Timber.d("Service disconnected");
            service.postValue(null);
        }
    };

    private final IConnectionDataInfoCallback connectionDataInfoCallback = new IConnectionDataInfoCallback.Stub() {
        @Override
        public void onConnectionDataInfo(DeviceId deviceId, ConnectionDataInfo connectionDataInfo) {
            connectionStatus.postValue(connectionDataInfo.getConnectionStatus());
            Map<DataType, DataInfo> dataMap = connectionDataInfo.getDataMap();
            if (dataMap != null) {
                postDataIfAvailable(dataMap, DataType.MANUFACTURER_INFO, manufacturerInfo);
                postDataIfAvailable(dataMap, DataType.SHIFTING, shiftingInfo);
                postDataIfAvailable(dataMap, DataType.BATTERY, batteryInfo);
                postDataIfAvailable(dataMap, DataType.SWITCH, switchEvent);
                postDataIfAvailable(dataMap, DataType.SIGNAL, signalInfo);
            }
        }
    };

    @SuppressWarnings("unchecked")
    private <TData> void postDataIfAvailable(Map<DataType, DataInfo> dataMap, DataType dataType, MutableLiveData<TData> mutableLiveData) {
        DataInfo dataInfo;
        dataInfo = dataMap.get(dataType);
        if (dataInfo != null && dataInfo.getValue() != null) {
            mutableLiveData.postValue((TData) dataInfo.getValue());
        }
    }

    private DeviceId deviceId;
    private final MutableLiveData<IKi2Service> service;
    private final MutableLiveData<ConnectionStatus> connectionStatus;
    private final MutableLiveData<ManufacturerInfo> manufacturerInfo;
    private final MutableLiveData<ShiftingInfo> shiftingInfo;
    private final MutableLiveData<BatteryInfo> batteryInfo;
    private final MutableLiveData<SwitchEvent> switchEvent;
    private final MutableLiveData<SignalInfo> signalInfo;

    public DeviceDetailsViewModel() {
        service = new MutableLiveData<>();
        connectionStatus = new MutableLiveData<>();
        manufacturerInfo = new MutableLiveData<>();
        shiftingInfo = new MutableLiveData<>();
        batteryInfo = new MutableLiveData<>();
        switchEvent = new MutableLiveData<>();
        signalInfo = new MutableLiveData<>();
    }

    public DeviceId getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(DeviceId deviceId) {
        this.deviceId = deviceId;
    }

    public ServiceConnection getServiceConnection() {
        return serviceConnection;
    }

    public LiveData<IKi2Service> getService() {
        return service;
    }

    public LiveData<ConnectionStatus> getConnectionStatus() {
        return connectionStatus;
    }

    public LiveData<ManufacturerInfo> getManufacturerInfo() {
        return manufacturerInfo;
    }

    public LiveData<ShiftingInfo> getShiftingInfo() {
        return shiftingInfo;
    }

    public LiveData<BatteryInfo> getBatteryInfo() {
        return batteryInfo;
    }

    public LiveData<SwitchEvent> getSwitchEvent() {
        return switchEvent;
    }

    public LiveData<SignalInfo> getSignalInfo() {
        return signalInfo;
    }

    public void reconnect() throws Exception {
        IKi2Service service = this.service.getValue();
        if (service == null) {
            throw new Exception("Service is not ready");
        }

        service.reconnectDevice(deviceId);
    }


    public void stopDataFlow() {
        IKi2Service service = this.service.getValue();
        if (service == null) {
            return;
        }

        try {
            service.unregisterConnectionDataInfoListener(connectionDataInfoCallback);
        } catch (Exception e) {
            Timber.e(e, "Unable to unregister data info callback");
        }
    }

    public void startDataFlow() throws Exception {
        IKi2Service service = this.service.getValue();
        if (service == null) {
            throw new Exception("Service is not ready");
        }

        service.registerConnectionDataInfoListener(connectionDataInfoCallback);
    }

    public void remove() throws Exception {
        IKi2Service service = this.service.getValue();
        if (service == null) {
            throw new Exception("Service is not ready");
        }

        service.deleteDevice(deviceId);
    }

    public void changeShiftingMode() throws Exception {
        IKi2Service service = this.service.getValue();
        if (service == null) {
            throw new Exception("Service is not ready");
        }

        service.changeShiftMode(deviceId);
    }
}