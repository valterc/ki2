package com.valterc.ki2.fragments.devices.gearing;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.preferences.device.DevicePreferences;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.services.IKi2Service;
import com.valterc.ki2.services.callbacks.IShiftingCallback;

import timber.log.Timber;

public class DeviceGearingViewModel extends ViewModel {

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Timber.d("Service connected");

            IKi2Service ki2Service = IKi2Service.Stub.asInterface(binder);
            service.postValue(ki2Service);

            try {
                ki2Service.registerShiftingListener(shiftingCallback);
            } catch (RemoteException e) {
                Timber.e("Unable to register shifting info listener");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Timber.d("Service disconnected");
            service.postValue(null);
        }
    };

    private final IShiftingCallback shiftingCallback = new IShiftingCallback.Stub() {
        @Override
        public void onShifting(DeviceId deviceId, ShiftingInfo shiftingInfo) {
            if (deviceId == null || !deviceId.equals(getDeviceId())) {
                return;
            }

            shiftingInfoData.postValue(shiftingInfo);
        }
    };

    private DeviceId deviceId;
    private final MutableLiveData<IKi2Service> service;
    private final MutableLiveData<ShiftingInfo> shiftingInfoData;

    public DeviceGearingViewModel() {
        this.service = new MutableLiveData<>();
        this.shiftingInfoData = new MutableLiveData<>();
    }

    public DeviceId getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(DeviceId deviceId) {
        this.deviceId = deviceId;
    }

    public DevicePreferences getDevicePreferences(Context context) {
        return new DevicePreferences(context, deviceId);
    }

    public ServiceConnection getServiceConnection() {
        return serviceConnection;
    }

    public LiveData<IKi2Service> getService() {
        return service;
    }

    public LiveData<ShiftingInfo> getShiftingInfo() {
        return shiftingInfoData;
    }

    public void stopDataFlow() {
        IKi2Service service = this.service.getValue();
        if (service == null) {
            return;
        }

        try {
            service.unregisterShiftingListener(shiftingCallback);
        } catch (Exception e) {
            Timber.e(e, "Unable to unregister callback");
        }
    }

}