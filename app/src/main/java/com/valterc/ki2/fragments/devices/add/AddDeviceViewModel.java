package com.valterc.ki2.fragments.devices;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import timber.log.Timber;

public class AddDeviceViewModel extends ViewModel {

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
            scanning.postValue(false);
        }
    };

    private final IScanCallback.Stub scanCallback = new IScanCallback.Stub() {
        @Override
        public void onScanResult(DeviceId deviceId) {
            Set<DeviceId> deviceSet = devices.getValue();

            if (deviceSet == null) {
                deviceSet = new HashSet<>();
            }

            boolean added = deviceSet.add(deviceId);
            if (added) {
                devices.postValue(deviceSet);
            }
        }
    };

    private final ExecutorService executorService;
    private final MutableLiveData<IKi2Service> service;
    private final MutableLiveData<Set<DeviceId>> devices;
    private final MutableLiveData<Boolean> scanning;

    public AddDeviceViewModel() {
        this.executorService = Executors.newFixedThreadPool(1);
        this.service = new MutableLiveData<>();
        this.devices = new MutableLiveData<>(new HashSet<>());
        this.scanning = new MutableLiveData<>(false);
    }

    public ServiceConnection getServiceConnection() {
        return serviceConnection;
    }

    public LiveData<IKi2Service> getService() {
        return service;
    }

    public LiveData<Set<DeviceId>> getDevices() {
        return devices;
    }

    public MutableLiveData<Boolean> getScanning() {
        return scanning;
    }

    public void startScan() throws Exception {
        IKi2Service service = this.service.getValue();
        if (service == null) {
            throw new Exception("Service is not ready");
        }

        scanning.setValue(true);
        executorService.submit(() -> {
            try {
                service.registerScanListener(scanCallback);
            } catch (RemoteException e) {
                Timber.e(e, "Unable to start device scan");
                scanning.postValue(false);
            }
        });
    }

    public void stopScan() {
        IKi2Service service = this.service.getValue();
        if (service == null) {
            return;
        }

        try {
            scanning.setValue(false);
            service.unregisterScanListener(scanCallback);
        } catch (RemoteException e) {
            Timber.e(e, "Unable to stop device scan");
        }
    }

}