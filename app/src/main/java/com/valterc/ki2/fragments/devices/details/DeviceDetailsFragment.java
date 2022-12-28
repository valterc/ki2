package com.valterc.ki2.fragments.devices.details;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.valterc.ki2.R;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.switches.SwitchType;
import com.valterc.ki2.fragments.IKarooKeyListener;
import com.valterc.ki2.data.input.KarooKey;
import com.valterc.ki2.services.Ki2Service;
import com.valterc.ki2.views.DrivetrainView;

import timber.log.Timber;

public class DeviceDetailsFragment extends Fragment implements IKarooKeyListener {

    private static final int ENABLE_REMOVE_BUTTON_DELAY_MS = 1000;
    private static final int SWITCH_AUTO_CLEAR_DELAY_MS = 1000;

    private final Handler handler = new Handler();
    private boolean serviceBound;
    private DeviceDetailsViewModel viewModel;
    private long timestampLeftSwitch;
    private long timestampRightSwitch;

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

        DeviceId deviceId = requireArguments().getParcelable(DeviceId.class.getSimpleName());

        if (deviceId == null) {
            throw new IllegalStateException(DeviceId.class.getSimpleName() + " not present in fragment arguments");
        }

        viewModel = new ViewModelProvider(this).get(DeviceDetailsViewModel.class);
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_device_details, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView textViewName = view.findViewById(R.id.textview_device_details_name);
        textViewName.setText(getString(R.string.text_param_di2_name, viewModel.getDeviceId().getName()));

        TextView textViewConnectionStatus = view.findViewById(R.id.textview_device_details_connection_status);
        textViewConnectionStatus.setText(R.string.text_connecting);

        Button buttonReconnect = view.findViewById(R.id.button_device_details_reconnect);
        buttonReconnect.setOnClickListener(v -> {
            try {
                viewModel.reconnect();
                buttonReconnect.setVisibility(ViewGroup.GONE);
                textViewConnectionStatus.setText(R.string.text_connecting);
                textViewConnectionStatus.setTextColor(requireContext().getColor(R.color.hh_black));
            } catch (Exception e) {
                Timber.e(e, "Unable to reconnect");
                Toast.makeText(requireContext(), R.string.text_unable_to_connect, Toast.LENGTH_SHORT).show();
            }
        });

        TextView textViewSignal = view.findViewById(R.id.textview_device_details_signal);

        LinearLayout linearLayoutData = view.findViewById(R.id.linearlayout_device_details_data);

        LinearLayout linearLayoutWaitingForDataBattery = view.findViewById(R.id.linearlayout_device_details_waiting_data_battery);
        TextView textViewBattery = view.findViewById(R.id.textview_device_details_battery);

        LinearLayout linearLayoutWaitingForDataManufacturer = view.findViewById(R.id.linearlayout_device_details_waiting_data_manufacturer);
        TextView textViewManufacturer = view.findViewById(R.id.textview_device_details_manufacturer_name);
        TextView textViewSerialNumber = view.findViewById(R.id.textview_device_details_serial_number);
        TextView textViewModel = view.findViewById(R.id.textview_device_details_model);
        TextView textViewSoftwareVersion = view.findViewById(R.id.textview_device_details_software_version);
        TextView textViewHardwareVersion = view.findViewById(R.id.textview_device_details_hardware_version);

        LinearLayout linearLayoutWaitingForDataShifting = view.findViewById(R.id.linearlayout_device_details_waiting_data_shifting);
        DrivetrainView drivetrainViewGears = view.findViewById(R.id.drivetrainview_device_details_gears);
        TextView textViewRearGear = view.findViewById(R.id.textview_device_details_rear_gear);
        TextView textViewFrontGear = view.findViewById(R.id.textview_device_details_front_gear);
        TextView textViewShiftingMode = view.findViewById(R.id.textview_device_details_shifting_mode);
        Button buttonChangeShiftingMode = view.findViewById(R.id.button_device_details_change_shifting_mode);
        buttonChangeShiftingMode.setOnClickListener(v -> {
            try {
                viewModel.changeShiftingMode();
            } catch (Exception e) {
                Timber.e(e, "Unable to change shifting mode");
                Toast.makeText(requireContext(), R.string.text_unable_to_change_shifting_mode, Toast.LENGTH_SHORT).show();
            }
        });

        TextView textViewLeftSwitch = view.findViewById(R.id.textview_device_details_left_switch);
        TextView textViewRightSwitch = view.findViewById(R.id.textview_device_details_right_switch);

        Button buttonRemove = view.findViewById(R.id.button_device_details_remove);
        buttonRemove.setOnClickListener(v -> new AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle)
                .setTitle(R.string.text_remove)
                .setMessage(getString(R.string.text_param_question_remove, getString(R.string.text_param_di2_name, viewModel.getDeviceId().getName())))
                .setIcon(R.drawable.ic_delete)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) ->
                {
                    try {
                        viewModel.remove();
                        requireActivity().finish();
                    } catch (Exception e) {
                        Timber.e(e, "Unable to remove");
                        Toast.makeText(getContext(), R.string.text_unable_to_remove, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.no, null).show());
        handler.postDelayed(() -> buttonRemove.setEnabled(true), ENABLE_REMOVE_BUTTON_DELAY_MS);

        viewModel.getService().observe(getViewLifecycleOwner(), service -> {
            if (service == null) {
                textViewConnectionStatus.setText(R.string.text_failed);
                Toast.makeText(requireContext(), R.string.text_unable_to_communicate_with_service, Toast.LENGTH_SHORT).show();
            } else {
                try {
                    viewModel.startDataFlow();
                } catch (Exception e) {
                    Timber.e(e, "Unable to start data flow");
                    textViewConnectionStatus.setText(R.string.text_failed);
                    Toast.makeText(requireContext(), R.string.text_unable_to_communicate_with_service, Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewModel.getConnectionStatus().observe(getViewLifecycleOwner(), connectionStatus -> {
            switch (connectionStatus) {

                case INVALID:
                    textViewConnectionStatus.setText(R.string.text_failed);
                    textViewConnectionStatus.setTextColor(requireContext().getColor(R.color.hh_red));
                    break;

                case NEW:
                case CONNECTING:
                    buttonReconnect.setVisibility(ViewGroup.GONE);
                    textViewConnectionStatus.setText(R.string.text_connecting);
                    textViewConnectionStatus.setTextColor(requireContext().getColor(R.color.hh_black));
                    break;

                case ESTABLISHED:
                    buttonReconnect.setVisibility(ViewGroup.GONE);
                    textViewConnectionStatus.setText(R.string.text_connected);
                    textViewConnectionStatus.setTextColor(requireContext().getColor(R.color.hh_black));
                    linearLayoutData.setVisibility(View.VISIBLE);
                    break;

                case CLOSED:
                    textViewConnectionStatus.setText(R.string.text_not_found);
                    textViewConnectionStatus.setTextColor(requireContext().getColor(R.color.hh_red));
                    textViewSignal.setText(R.string.text_na);
                    buttonReconnect.setVisibility(ViewGroup.VISIBLE);
                    break;

            }
        });

        viewModel.getSignalInfo().observe(getViewLifecycleOwner(), signalInfo -> textViewSignal.setText(getString(R.string.text_param_dbm, signalInfo.getValue())));

        viewModel.getBatteryInfo().observe(getViewLifecycleOwner(), batteryInfo -> {
            int batteryValue = batteryInfo.getValue();
            textViewBattery.setText(getString(R.string.text_param_percentage, batteryValue));

            int color;
            int drawableId;

            if (batteryValue >= 80) {
                color = requireContext().getColor(R.color.hh_green);
                drawableId = R.drawable.ic_battery_5;
            } else if (batteryValue >= 70) {
                color = requireContext().getColor(R.color.hh_green);
                drawableId = R.drawable.ic_battery_4;
            } else if (batteryValue >= 50) {
                color = requireContext().getColor(R.color.hh_green);
                drawableId = R.drawable.ic_battery_3;
            } else if (batteryValue >= 30) {
                color = requireContext().getColor(R.color.hh_yellow_dark);
                drawableId = R.drawable.ic_battery_2;
            } else if (batteryValue >= 20) {
                color = requireContext().getColor(R.color.hh_orange_dark);
                drawableId = R.drawable.ic_battery_1;
            } else {
                color = requireContext().getColor(R.color.hh_red);
                drawableId = R.drawable.ic_battery_0;
            }

            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(textViewBattery, 0, 0, drawableId, 0);
            TextViewCompat.setCompoundDrawableTintList(textViewBattery, ColorStateList.valueOf(color));
            linearLayoutWaitingForDataBattery.setVisibility(View.GONE);
        });

        viewModel.getManufacturerInfo().observe(getViewLifecycleOwner(), manufacturerInfo -> {
            textViewManufacturer.setText(manufacturerInfo.getManufacturer().getName());
            textViewSerialNumber.setText(manufacturerInfo.getSerialNumber());
            textViewModel.setText(manufacturerInfo.getModelNumber());
            textViewSoftwareVersion.setText(manufacturerInfo.getSoftwareVersion());
            textViewHardwareVersion.setText(manufacturerInfo.getHardwareVersion());
            linearLayoutWaitingForDataManufacturer.setVisibility(View.GONE);
        });

        viewModel.getShiftingInfo().observe(getViewLifecycleOwner(), shiftingInfo -> {
            drivetrainViewGears.setRearGearMax(shiftingInfo.getRearGearMax());
            drivetrainViewGears.setFrontGearMax(shiftingInfo.getFrontGearMax());
            drivetrainViewGears.setRearGear(shiftingInfo.getRearGear());
            drivetrainViewGears.setFrontGear(shiftingInfo.getFrontGear());

            textViewRearGear.setText(shiftingInfo.getRearGear() + "/" + shiftingInfo.getRearGearMax());
            textViewFrontGear.setText(shiftingInfo.getFrontGear() + "/" + shiftingInfo.getFrontGearMax());
            textViewShiftingMode.setText(shiftingInfo.getShiftingMode().getMode());
            linearLayoutWaitingForDataShifting.setVisibility(View.GONE);
        });

        viewModel.getSwitchEvent().observe(getViewLifecycleOwner(), switchEvent -> {
            TextView textViewSwitch;

            if (switchEvent.getType() == SwitchType.LEFT) {
                textViewSwitch = textViewLeftSwitch;
                timestampLeftSwitch = System.currentTimeMillis();
            } else {
                textViewSwitch = textViewRightSwitch;
                timestampRightSwitch = System.currentTimeMillis();
            }

            boolean autoClear = false;

            switch (switchEvent.getCommand()) {
                case LONG_PRESS_UP:
                case NO_SWITCH:
                    textViewSwitch.setText(R.string.text_no_action);
                    break;

                case SINGLE_CLICK:
                    textViewSwitch.setText(R.string.text_single_press);
                    autoClear = true;
                    break;

                case DOUBLE_CLICK:
                    textViewSwitch.setText(R.string.text_double_press);
                    autoClear = true;
                    break;

                case LONG_PRESS_DOWN:
                case LONG_PRESS_CONTINUE:
                    textViewSwitch.setText(R.string.text_holding);
                    break;
            }

            if (autoClear) {
                handler.postDelayed(() -> {
                    if (switchEvent.getType() == SwitchType.LEFT) {
                        if (System.currentTimeMillis() - timestampLeftSwitch > SWITCH_AUTO_CLEAR_DELAY_MS * 0.8) {
                            textViewLeftSwitch.setText(R.string.text_no_action);
                        }
                    } else {
                        if (System.currentTimeMillis() - timestampRightSwitch > SWITCH_AUTO_CLEAR_DELAY_MS * 0.8) {
                            textViewRightSwitch.setText(R.string.text_no_action);
                        }
                    }
                }, SWITCH_AUTO_CLEAR_DELAY_MS);
            }

        });

    }

    @Override
    public boolean onKarooKeyPressed(KarooKey karooKey) {
        return false;
    }
}