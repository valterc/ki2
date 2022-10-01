package com.valterc.ki2.fragments.devices;

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

public class AddDeviceViewModel extends ViewModel {

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Timber.d("Service connected");
            service.postValue((IKi2Service) binder);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Timber.d("Service disconnected");
            service.postValue(null);
        }
    };

    private MutableLiveData<IKi2Service> service;
    private MutableLiveData<List<DeviceId>> devices;


    public AddDeviceViewModel() {
        this.service = new MutableLiveData<>();
        this.devices = new MutableLiveData<>();
    }

    public ServiceConnection getServiceConnection() {
        return serviceConnection;
    }

    public LiveData<IKi2Service> getService() {
        return service;
    }

    public LiveData<List<DeviceId>> getDevices() {
        return devices;
    }

    public void refreshDevices() {
        IKi2Service service = this.service.getValue();
        if (service != null) {

        }

        service.
    }
}