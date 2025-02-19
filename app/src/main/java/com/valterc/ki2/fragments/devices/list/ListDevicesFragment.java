package com.valterc.ki2.fragments.devices.list;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.valterc.ki2.R;
import com.valterc.ki2.activities.devices.add.AddDeviceActivity;
import com.valterc.ki2.activities.devices.details.DeviceDetailsActivity;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.input.KarooKey;
import com.valterc.ki2.data.preferences.device.DevicePreferences;
import com.valterc.ki2.fragments.IKarooKeyListener;
import com.valterc.ki2.services.Ki2Service;

import java.util.List;

import timber.log.Timber;

public class ListDevicesFragment extends Fragment implements IKarooKeyListener {

    private TextView textViewNoSavedDevices;
    private ListDevicesViewModel viewModel;
    private ListDevicesAdapter listDevicesAdapter;
    private ItemTouchHelper itemTouchHelper;
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
        return inflater.inflate(R.layout.fragment_devices_list, container, false);
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
        }, this::startDragging);

        viewModel.getService().observe(getViewLifecycleOwner(), service -> {
            if (service != null) {
                try {
                    List<DeviceId> devices = viewModel.getSavedDevices();
                    devices.sort((a, b) -> {
                        DevicePreferences devicePreferencesA = new DevicePreferences(requireContext(), a);
                        DevicePreferences devicePreferencesB = new DevicePreferences(requireContext(), b);

                        int priority = devicePreferencesA.getPriority() - devicePreferencesB.getPriority();
                        if (priority != 0) {
                            return priority;
                        }

                        return devicePreferencesA.getName().compareToIgnoreCase(devicePreferencesB.getName());
                    });
                    listDevicesAdapter.setDevices(devices);
                    textViewNoSavedDevices.setVisibility(viewModel.anyDevicesSaved() ? View.GONE : View.VISIBLE);
                    viewModel.startReceivingData();
                } catch (Exception e) {
                    Timber.e(e, "Unable to get devices and start receiving data");
                    Toast.makeText(getContext(), R.string.text_unable_to_communicate_with_service, Toast.LENGTH_LONG).show();
                }
            }
        });

        viewModel.getDeviceConnectionDataEvent().observe(getViewLifecycleOwner(), listDevicesAdapter::setConnectionDataInfo);
        recyclerView.setAdapter(listDevicesAdapter);

        buttonAddDevice.setOnClickListener(v -> startActivity(new Intent(getContext(), AddDeviceActivity.class)));

        ItemTouchHelper.SimpleCallback touchCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();

                listDevicesAdapter.moveDevicePosition(fromPosition, toPosition);
                updateDevicePriorities(listDevicesAdapter.getDevices());

                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }

            @Override
            public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);

                if (viewHolder != null && actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    viewHolder.itemView.setBackgroundResource(R.color.white);
                }
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);

                TypedValue outValue = new TypedValue();
                requireContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                viewHolder.itemView.setBackgroundResource(outValue.resourceId);
            }
        };

        itemTouchHelper = new ItemTouchHelper(touchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void startDragging(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    private void updateDevicePriorities(List<DeviceId> devices) {
        for (int i = 0; i < devices.size(); i++) {
            new DevicePreferences(requireContext(), devices.get(i)).setPriority(i);
        }
    }

    @Override
    public boolean onKarooKeyPressed(KarooKey karooKey) {
        if (karooKey == KarooKey.BOTTOM_RIGHT) {
            startActivity(new Intent(getContext(), AddDeviceActivity.class));
            return true;
        }

        return false;
    }
}
