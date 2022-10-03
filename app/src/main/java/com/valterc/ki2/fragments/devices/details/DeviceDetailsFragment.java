package com.valterc.ki2.fragments.devices.details;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.valterc.ki2.R;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.fragments.IKarooKeyListener;
import com.valterc.ki2.fragments.devices.add.AddDeviceViewModel;
import com.valterc.ki2.fragments.devices.add.SearchDevicesAdapter;
import com.valterc.ki2.karoo.input.KarooKey;
import com.valterc.ki2.services.Ki2Service;

import java.util.Set;

import timber.log.Timber;

public class DeviceDetailsFragment extends Fragment implements IKarooKeyListener {

    private boolean serviceBound;
    private DeviceDetailsViewModel viewModel;

    public static DeviceDetailsFragment newInstance(DeviceId deviceId) {
        DeviceDetailsFragment deviceDetailsFragment = new DeviceDetailsFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(DeviceId.class.getSimpleName(), deviceId);
        deviceDetailsFragment.setArguments(bundle);

        return deviceDetailsFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DeviceDetailsViewModel.class);
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
            requireContext().unbindService(viewModel.getServiceConnection());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_device_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel.getService().observe(getViewLifecycleOwner(), service -> {

        });
    }

    @Override
    public boolean onKarooKeyPressed(KarooKey karooKey) {
        return false;
    }
}