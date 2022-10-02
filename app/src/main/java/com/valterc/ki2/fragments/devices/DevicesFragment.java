package com.valterc.ki2.fragments.devices;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.valterc.ki2.R;
import com.valterc.ki2.activities.devices.AddDeviceActivity;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.fragments.IKarooKeyListener;
import com.valterc.ki2.karoo.input.KarooKey;
import com.valterc.ki2.services.IKi2Service;
import com.valterc.ki2.services.Ki2Service;

import java.util.List;

import timber.log.Timber;

public class DevicesFragment extends Fragment implements IKarooKeyListener {

    private DevicesViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DevicesViewModel.class);

        /*
        boolean result = getContext().bindService(serviceIntent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Toast.makeText(getContext(), "Service connected", Toast.LENGTH_SHORT).show();

                try {
                    IKi2Service ki2Service = (IKi2Service) service;
                    List<DeviceId> savedDevices = ki2Service.getSavedDevices();

                    Toast.makeText(getContext(), "Devices: " + savedDevices, Toast.LENGTH_SHORT).show();
                }catch (Exception e) {
                    Timber.w(e);
                }

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Toast.makeText(getContext(), "Service disconnected", Toast.LENGTH_SHORT).show();
            }
        }, Context.BIND_AUTO_CREATE | Context.BIND_INCLUDE_CAPABILITIES);

        Timber.d("Bind result: %s", result);
         */
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_devices, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ExtendedFloatingActionButton buttonAddDevice = view.findViewById(R.id.button_devices_add);
        buttonAddDevice.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), AddDeviceActivity.class));
        });
    }

    @Override
    public boolean onKarooKeyPressed(KarooKey karooKey) {
        if (karooKey == KarooKey.CONFIRM) {
            startActivity(new Intent(getContext(), AddDeviceActivity.class));
            return true;
        }

        return false;
    }
}
