package com.valterc.ki2.fragments.devices;

import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.valterc.ki2.R;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.fragments.KarooKeyListener;
import com.valterc.ki2.karoo.input.KarooKey;
import com.valterc.ki2.services.Ki2Service;

import java.util.List;
import java.util.Set;

import timber.log.Timber;

public class AddDeviceFragment extends Fragment implements KarooKeyListener {

    private static final long DURATION_SCAN_MS = 20_000;

    private final Handler handlerStopScan = new Handler();
    private boolean serviceBound;
    private long timestampScanStart;
    private AddDeviceViewModel viewModel;

    public static AddDeviceFragment newInstance() {
        return new AddDeviceFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AddDeviceViewModel.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        serviceBound = requireContext().bindService(Ki2Service.getIntent(), viewModel.getServiceConnection(), Context.BIND_AUTO_CREATE);
        if (!serviceBound) {
            Toast.makeText(getContext(), "Unable to communicate with service", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (serviceBound) {
            viewModel.stopScan();
            requireContext().unbindService(viewModel.getServiceConnection());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_device, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ExtendedFloatingActionButton buttonScan = view.findViewById(R.id.button_add_devices_scan);
        buttonScan.setOnClickListener(v -> startScan());

        TextView textViewNoDevicesFound = view.findViewById(R.id.textview_add_device_no_devices_found);
        TextView textViewTopMessage = view.findViewById(R.id.textview_add_device_top_message);
        CircularProgressIndicator progressIndicator = view.findViewById(R.id.progressindicator_add_devices_scanning);
        RecyclerView recyclerViewDevices = view.findViewById(R.id.recyclerview_add_devices_scan_results);

        viewModel.getScanning().observe(getViewLifecycleOwner(), scanning -> {
            buttonScan.setEnabled(!scanning);

            if (!scanning) {
                Set<DeviceId> deviceSet = viewModel.getDevices().getValue();
                if (deviceSet != null) {
                    int deviceCount = deviceSet.size();
                    if (deviceCount == 0) {
                        textViewNoDevicesFound.setVisibility(View.VISIBLE);
                    }
                    textViewTopMessage.setText(getString(R.string.text_param_found_devices, deviceCount));
                } else {
                    textViewTopMessage.setText(getString(R.string.text_unable_to_scan_devices));
                }
                progressIndicator.setVisibility(View.GONE);
            } else {
                textViewTopMessage.setText(getString(R.string.text_searching_for_devices));
                progressIndicator.setVisibility(View.VISIBLE);
                textViewNoDevicesFound.setVisibility(View.GONE);
            }
        });

        viewModel.getDevices().observe(getViewLifecycleOwner(), devices -> {
            Toast.makeText(getContext(), "Devices: " + devices, Toast.LENGTH_SHORT).show();
        });

        viewModel.getService().observe(getViewLifecycleOwner(), service -> {
            if (service != null) {
                startScan();
            } else if (Boolean.TRUE.equals(viewModel.getScanning().getValue())) {
                Toast.makeText(getContext(), "Service disconnected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startScan() {

        if (Boolean.TRUE.equals(viewModel.getScanning().getValue())) {
            return;
        }

        try {
            timestampScanStart = System.currentTimeMillis();
            viewModel.startScan();
            handlerStopScan.postDelayed(() -> {
                if (System.currentTimeMillis() - timestampScanStart >= DURATION_SCAN_MS) {
                    stopScan();
                }
            }, DURATION_SCAN_MS);
        } catch (Exception e) {
            Timber.e(e, "Unable to start device scan");
            Toast.makeText(getContext(), R.string.text_unable_to_scan_devices, Toast.LENGTH_SHORT).show();
        }
    }

    private void stopScan() {
        viewModel.stopScan();
    }

    @Override
    public boolean onKarooKeyPressed(KarooKey karooKey) {
        if (karooKey == KarooKey.CONFIRM) {
            startScan();
            return true;
        }

        return false;
    }
}