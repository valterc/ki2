package com.valterc.ki2.fragments.devices.list;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.services.IKi2Service;

import java.util.List;

import timber.log.Timber;

public class ListDevicesViewModel extends ViewModel {

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Timber.d("Service connected");
            service.postValue(IKi2Service.Stub.asInterface(binder));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Timber.d("Service disconnected");
            service.postValue(null);
        }
    };

    private final MutableLiveData<IKi2Service> service;

    private List<DeviceId> savedDeviceList;

    public ListDevicesViewModel() {
        this.service = new MutableLiveData<>();
    }

    public ServiceConnection getServiceConnection() {
        return serviceConnection;
    }

    public LiveData<IKi2Service> getService() {
        return service;
    }

    public List<DeviceId> getSavedDevices() throws Exception {

        if (savedDeviceList != null) {
            return savedDeviceList;
        }

        if (service.getValue() == null) {
            throw new Exception("Service is not ready");
        }

        savedDeviceList = service.getValue().getSavedDevices();
        return savedDeviceList;
    }

}