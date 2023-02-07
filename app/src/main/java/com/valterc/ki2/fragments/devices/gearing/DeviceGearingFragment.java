package com.valterc.ki2.fragments.devices.gearing;

import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.valterc.ki2.R;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.preferences.device.DevicePreferences;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.services.Ki2Service;

public class DeviceGearingFragment extends Fragment {

    private boolean serviceBound;
    private DeviceGearingViewModel viewModel;

    public static DeviceGearingFragment newInstance(DeviceId deviceId) {
        DeviceGearingFragment deviceGearingFragment = new DeviceGearingFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(DeviceId.class.getSimpleName(), deviceId);
        deviceGearingFragment.setArguments(bundle);

        return deviceGearingFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DeviceId deviceId = requireArguments().getParcelable(DeviceId.class.getSimpleName());

        if (deviceId == null) {
            throw new IllegalStateException(DeviceId.class.getSimpleName() + " not present in fragment arguments");
        }

        viewModel = new ViewModelProvider(this).get(DeviceGearingViewModel.class);
        viewModel.setDeviceId(deviceId);
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
            viewModel.stopDataFlow();
            requireContext().unbindService(viewModel.getServiceConnection());
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_device_gearing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DevicePreferences devicePreferences = viewModel.getDevicePreferences(requireContext());

        View viewCustomGearing = view.findViewById(R.id.linearlayout_device_gearing_custom);
        View viewAutoGearing = view.findViewById(R.id.linearlayout_device_gearing_auto);
        View viewAutoGearingData = view.findViewById(R.id.linearlayout_device_gearing_auto_data);

        MaterialCheckBox checkBoxAutoDetect = view.findViewById(R.id.checkbox_device_gearing_auto_detect);
        checkBoxAutoDetect.setChecked(devicePreferences.isGearingDetectedAutomatically());
        checkBoxAutoDetect.addOnCheckedStateChangedListener((checkBox, state) -> {
            devicePreferences.setGearingDetectedAutomatically(checkBox.isChecked());
            if (checkBox.isChecked()) {
                viewCustomGearing.setVisibility(View.GONE);
                viewAutoGearing.setVisibility(View.VISIBLE);
            } else {
                viewCustomGearing.setVisibility(View.VISIBLE);
                viewAutoGearing.setVisibility(View.GONE);
            }
        });

        if (checkBoxAutoDetect.isChecked()) {
            viewCustomGearing.setVisibility(View.GONE);
            viewAutoGearing.setVisibility(View.VISIBLE);
        } else {
            viewCustomGearing.setVisibility(View.VISIBLE);
            viewAutoGearing.setVisibility(View.GONE);
        }

        TextView textViewAutoFront = view.findViewById(R.id.textview_device_gearing_auto_front);
        TextView textViewAutoRear = view.findViewById(R.id.textview_device_gearing_auto_rear);

        ShiftingInfo shiftingInfo = viewModel.getShiftingInfo().getValue();
        if (shiftingInfo != null) {
            viewAutoGearingData.setVisibility(View.VISIBLE);
        } else {
            viewAutoGearingData.setVisibility(View.GONE);
        }

        viewModel.getShiftingInfo().observe(getViewLifecycleOwner(), s -> {
            if (viewModel.getDevicePreferences(requireContext()).isGearingDetectedAutomatically()) {
                viewAutoGearingData.setVisibility(View.VISIBLE);
            }

            textViewAutoFront.setText(s.getFrontTeethPattern().getName());
            textViewAutoRear.setText(s.getRearTeethPattern().getName());
        });

    }

}