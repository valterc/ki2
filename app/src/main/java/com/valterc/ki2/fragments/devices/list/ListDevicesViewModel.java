package com.valterc.ki2.fragments.devices.list;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.valterc.ki2.data.connection.ConnectionDataInfo;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.services.IKi2Service;
import com.valterc.ki2.services.callbacks.IConnectionDataInfoCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import timber.log.Timber;

public class ListDevicesViewModel extends ViewModel {

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service.postValue(IKi2Service.Stub.asInterface(binder));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            service.postValue(null);
        }
    };

    private final IConnectionDataInfoCallback connectionDataInfoCallback = new IConnectionDataInfoCallback.Stub() {
        @Override
        public void onConnectionDataInfo(DeviceId deviceId, ConnectionDataInfo connectionDataInfo) {
            Map<DeviceId, ConnectionDataInfo> connectionDataInfoMap = Objects.requireNonNull(deviceConnectionDataEvent.getValue());
            connectionDataInfoMap.put(deviceId, connectionDataInfo);
            deviceConnectionDataEvent.postValue(connectionDataInfoMap);
        }
    };

    private final MutableLiveData<IKi2Service> service;
    private final MutableLiveData<Map<DeviceId, ConnectionDataInfo>> deviceConnectionDataEvent;

    public ListDevicesViewModel() {
        this.service = new MutableLiveData<>();
        this.deviceConnectionDataEvent = new MutableLiveData<>(new HashMap<>());
    }

    public ServiceConnection getServiceConnection() {
        return serviceConnection;
    }

    public LiveData<IKi2Service> getService() {
        return service;
    }

    public LiveData<Map<DeviceId, ConnectionDataInfo>> getDeviceConnectionDataEvent() {
        return deviceConnectionDataEvent;
    }

    public boolean anyDevicesSaved() {
        try {
            return getSavedDevices().size() != 0;
        } catch (Exception e) {
            return false;
        }
    }

    public List<DeviceId> getSavedDevices() throws Exception {
        IKi2Service service = this.service.getValue();
        if (service == null) {
            throw new Exception("Service is not ready");
        }

        return service.getSavedDevices();
    }

    public void startReceivingData() throws Exception {
        IKi2Service service = this.service.getValue();
        if (service == null) {
            throw new Exception("Service is not ready");
        }

        try {
            service.registerConnectionDataInfoListener(connectionDataInfoCallback);
        } catch (RemoteException e) {
            Timber.e(e, "Unable to register callback");
            throw new Exception("Unable to connect to devices", e);
        }
    }

    public void stopReceivingData() {
        IKi2Service service = this.service.getValue();
        if (service == null) {
            return;
        }

        try {
            service.unregisterConnectionDataInfoListener(connectionDataInfoCallback);
        } catch (RemoteException e) {
            Timber.e(e, "Unable to unregister callback");
        }
    }

    public void reconnect(DeviceId deviceId) throws Exception {
        IKi2Service service = this.service.getValue();
        if (service == null) {
            throw new Exception("Service is not ready");
        }

        try {
            service.reconnectDevice(deviceId);
        } catch (RemoteException e) {
            Timber.e(e, "Unable to reconnect to device");
            throw new Exception("Unable to reconnect to device", e);
        }
    }
}