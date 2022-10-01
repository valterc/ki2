package com.valterc.ki2.fragments.devices;

import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.valterc.ki2.R;
import com.valterc.ki2.activities.devices.AddDeviceActivity;

public class AddDeviceFragment extends Fragment {

    private AddDeviceViewModel mViewModel;

    public static AddDeviceFragment newInstance() {
        return new AddDeviceFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(AddDeviceViewModel.class);

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

        ExtendedFloatingActionButton buttonAddDevice = view.findViewById(R.id.button_add_devices_refresh);
        buttonAddDevice.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), AddDeviceActivity.class));
        });

    }
}