package com.valterc.ki2.fragments.devices.details;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.services.IKi2Service;
import com.valterc.ki2.services.callbacks.IScanCallback;

import java.util.HashSet;
import java.util.Set;

import timber.log.Timber;

public class DeviceDetailsViewModel extends ViewModel {

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

    public DeviceDetailsViewModel() {
        this.service = new MutableLiveData<>();
    }

    public ServiceConnection getServiceConnection() {
        return serviceConnection;
    }

    public LiveData<IKi2Service> getService() {
        return service;
    }

}