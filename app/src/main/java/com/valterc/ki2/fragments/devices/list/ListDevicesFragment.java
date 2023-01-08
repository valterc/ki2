package com.valterc.ki2.fragments.devices.list;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.valterc.ki2.R;
import com.valterc.ki2.activities.devices.add.AddDeviceActivity;
import com.valterc.ki2.activities.devices.details.DeviceDetailsActivity;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.input.KarooKey;
import com.valterc.ki2.fragments.IKarooKeyListener;
import com.valterc.ki2.services.Ki2Service;

import java.util.List;

import timber.log.Timber;

public class ListDevicesFragment extends Fragment implements IKarooKeyListener {

    private TextView textViewNoSavedDevices;
    private ListDevicesViewModel viewModel;
    private ListDevicesAdapter listDevicesAdapter;
    private boolean serviceBound;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ListDevicesViewModel.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        serviceBound = requireContext().bindService(Ki2Service.getIntent(), viewModel.getServiceConnection(), Context.BIND_AUTO_CREATE);
        if (!serviceBound) {
            Toast.makeText(getContext(), R.string.text_unable_to_communicate_with_service, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (serviceBound) {
            serviceBound = false;
            viewModel.stopReceivingData();
            requireContext().unbindService(viewModel.getServiceConnection());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_devices, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textViewNoSavedDevices = view.findViewById(R.id.textview_list_devices_no_devices);
        ExtendedFloatingActionButton buttonAddDevice = view.findViewById(R.id.button_list_devices_add);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview_list_devices);
        listDevicesAdapter = new ListDevicesAdapter(requireContext(), deviceId -> {
            Intent intent = new Intent(getContext(), DeviceDetailsActivity.class);
            intent.putExtra(DeviceId.class.getSimpleName(), deviceId);
            startActivity(intent);
        }, deviceId -> {
            try {
                viewModel.reconnect(deviceId);
            } catch (Exception e) {
                Timber.e(e, "Unable to reconnect to device");
                Toast.makeText(getContext(), R.string.text_unable_to_communicate_with_service, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getService().observe(getViewLifecycleOwner(), service -> {
            if (service != null) {
                try {
                    List<DeviceId> devices = viewModel.getSavedDevices();
                    listDevicesAdapter.setDevices(devices);
                    textViewNoSavedDevices.setVisibility(viewModel.anyDevicesSaved() ? View.GONE : View.VISIBLE);
                    viewModel.startReceivingData();
                } catch (Exception e) {
                    Timber.e(e, "Unable to get devices and start receiving data");
                    Toast.makeText(getContext(), R.string.text_unable_to_communicate_with_service, Toast.LENGTH_LONG).show();
                }
            }
        });

        viewModel.getDeviceConnectionDataEvent().observe(getViewLifecycleOwner(), listDevicesAdapter::addConnectionDataInfo);
        recyclerView.setAdapter(listDevicesAdapter);

        buttonAddDevice.setOnClickListener(v -> startActivity(new Intent(getContext(), AddDeviceActivity.class)));
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
