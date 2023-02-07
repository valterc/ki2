package com.valterc.ki2.fragments.devices.details;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.valterc.ki2.R;
import com.valterc.ki2.activities.devices.gearing.DeviceGearingActivity;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.input.KarooKey;
import com.valterc.ki2.data.preferences.device.DevicePreferences;
import com.valterc.ki2.data.shifting.FrontTeethPattern;
import com.valterc.ki2.data.shifting.RearTeethPattern;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.data.switches.SwitchType;
import com.valterc.ki2.fragments.IKarooKeyListener;
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

        ImageView imageViewIcon = view.findViewById(R.id.imageview_device_details_icon);
        switch (viewModel.getDeviceId().getDeviceType()) {
            case SHIMANO_SHIFTING:
                imageViewIcon.setImageResource(R.drawable.ic_di2);
                break;

            case SHIMANO_EBIKE:
                imageViewIcon.setImageResource(R.drawable.ic_steps);
                break;

            case MOCK_SHIFTING:
                imageViewIcon.setImageResource(R.drawable.ic_mock);
                break;

            case UNKNOWN:
            default:
                imageViewIcon.setImageResource(R.drawable.ic_memory);
                break;
        }

        TextView textViewName = view.findViewById(R.id.textview_device_details_name);
        textViewName.setText(viewModel.getDevicePreferences(requireContext()).getName());

        TextView textViewConnectionStatus = view.findViewById(R.id.textview_device_details_connection_status);
        textViewConnectionStatus.setText(R.string.text_connecting);

        Button buttonReconnect = view.findViewById(R.id.button_device_details_reconnect);
        buttonReconnect.setOnClickListener(v -> {
            try {
                viewModel.reconnect();
                buttonReconnect.setEnabled(false);
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
                .setMessage(getString(R.string.text_param_question_remove, viewModel.getDevicePreferences(requireContext()).getName()))
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

        Button buttonRename = view.findViewById(R.id.button_device_details_rename);
        buttonRename.setOnClickListener(v -> {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_device_rename, null);
            final EditText editText = dialogView.findViewById(R.id.edittext_dialog_device_name);
            editText.setText(viewModel.getDevicePreferences(requireContext()).getName());
            editText.setSelection(editText.getText().length());

            new AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle)
                    .setTitle(R.string.text_rename)
                    .setIcon(R.drawable.ic_edit)
                    .setView(dialogView)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, (dialog, whichButton) -> {
                        String newName = editText.getText().toString();
                        viewModel.getDevicePreferences(requireContext()).setName(newName);
                        textViewName.setText(viewModel.getDevicePreferences(requireContext()).getName());
                    }).show();
        });

        DevicePreferences devicePreferences = viewModel.getDevicePreferences(requireContext());

        MaterialCheckBox checkBoxEnabled = view.findViewById(R.id.checkbox_device_details_enabled);
        checkBoxEnabled.setChecked(devicePreferences.isEnabled());
        checkBoxEnabled.addOnCheckedStateChangedListener((checkBox, state) -> {
            if (checkBox.isChecked()) {
                viewModel.getDevicePreferences(requireContext()).setEnabled(true);
            } else {
                new AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle)
                        .setTitle(R.string.text_question_device_disable)
                        .setMessage(getString(R.string.text_device_disable))
                        .setPositiveButton(R.string.text_disable, (dialog, whichButton) ->
                        {
                            viewModel.getDevicePreferences(requireContext()).setEnabled(false);

                            textViewConnectionStatus.setText(R.string.text_disabled);
                            textViewConnectionStatus.setTextColor(requireContext().getColor(R.color.hh_dark_grey));
                            textViewSignal.setText(R.string.text_na);
                            buttonReconnect.setEnabled(false);
                            linearLayoutData.setVisibility(View.GONE);
                        })
                        .setNegativeButton(R.string.text_cancel, (dialog, whichButton) ->
                                checkBox.setChecked(true)).show();
            }
        });

        MaterialCheckBox checkBoxSwitchEventsOnly = view.findViewById(R.id.checkbox_device_details_switch_only);
        checkBoxSwitchEventsOnly.setChecked(devicePreferences.isSwitchEventsOnly());
        checkBoxSwitchEventsOnly.addOnCheckedStateChangedListener((checkBox, state) -> {
            if (!checkBox.isChecked()) {
                viewModel.getDevicePreferences(requireContext()).setSwitchEventsOnly(false);
            } else {
                new AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle)
                        .setTitle(R.string.text_switches_only)
                        .setMessage(getString(R.string.text_device_switches_only))
                        .setPositiveButton(android.R.string.ok, (dialog, whichButton) ->
                                viewModel.getDevicePreferences(requireContext()).setSwitchEventsOnly(true))
                        .setNegativeButton(R.string.text_cancel, (dialog, whichButton) ->
                                checkBox.setChecked(false)).show();
            }
        });

        View viewGearing = view.findViewById(R.id.constrainglayout_device_details_gearing);
        viewGearing.setOnClickListener(v -> startActivity(new Intent(requireContext(), DeviceGearingActivity.class)
                .putExtra(DeviceId.class.getSimpleName(), viewModel.getDeviceId())));

        TextView textViewGearing = view.findViewById(R.id.textview_device_details_gearing);
        setGearingText(textViewGearing, devicePreferences, null);

        if (!devicePreferences.isEnabled()) {
            textViewConnectionStatus.setText(R.string.text_disabled);
            textViewConnectionStatus.setTextColor(requireContext().getColor(R.color.hh_dark_grey));
            textViewSignal.setText(R.string.text_na);
            buttonReconnect.setEnabled(false);
            linearLayoutData.setVisibility(View.GONE);
        }

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
                    buttonReconnect.setEnabled(false);
                    textViewConnectionStatus.setText(R.string.text_connecting);
                    textViewConnectionStatus.setTextColor(requireContext().getColor(R.color.hh_black));
                    textViewSignal.setText(R.string.text_na);
                    break;

                case ESTABLISHED:
                    buttonReconnect.setEnabled(false);
                    textViewConnectionStatus.setText(R.string.text_connected);
                    textViewConnectionStatus.setTextColor(requireContext().getColor(R.color.hh_black));
                    linearLayoutData.setVisibility(View.VISIBLE);
                    break;

                case CLOSED:
                    if (viewModel.getDevicePreferences(requireContext()).isEnabled()) {
                        textViewConnectionStatus.setText(R.string.text_not_found);
                        textViewConnectionStatus.setTextColor(requireContext().getColor(R.color.hh_red));
                        textViewSignal.setText(R.string.text_na);
                        buttonReconnect.setEnabled(true);
                    } else {
                        textViewConnectionStatus.setText(R.string.text_disabled);
                        textViewConnectionStatus.setTextColor(requireContext().getColor(R.color.hh_dark_grey));
                        textViewSignal.setText(R.string.text_na);
                        buttonReconnect.setEnabled(false);
                        linearLayoutData.setVisibility(View.GONE);
                    }
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

            setGearingText(textViewGearing, viewModel.getDevicePreferences(requireContext()), shiftingInfo);
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

    private void setGearingText(TextView textView, DevicePreferences devicePreferences, ShiftingInfo shiftingInfo) {
        if (devicePreferences.isGearingDetectedAutomatically()) {
            if (shiftingInfo == null) {
                textView.setText(R.string.text_detect_automatically);
            } else {
                FrontTeethPattern frontTeethPattern = shiftingInfo.getFrontTeethPattern();
                RearTeethPattern rearTeethPattern = shiftingInfo.getRearTeethPattern();
                textView.setText(getString(R.string.text_param_gearing_detect_automatically,
                        frontTeethPattern.getName(), rearTeethPattern.getName()));
            }
        } else {
            int[] frontGearing = devicePreferences.getCustomGearingFront();
            int[] rearGearing = devicePreferences.getCustomGearingRear();
            String frontGearingString;
            String rearGearingString;

            if (frontGearing != null) {
                frontGearingString = frontGearing.length == 1 ? String.valueOf(frontGearing[0]) : frontGearing[0] + "-" + frontGearing[frontGearing.length - 1];
            } else {
                frontGearingString = getString(R.string.text_unknown);
            }

            if (rearGearing != null) {
                rearGearingString = rearGearing.length == 1 ? String.valueOf(rearGearing[0]) : rearGearing[0] + "-" + rearGearing[rearGearing.length - 1];
            } else {
                rearGearingString = getString(R.string.text_unknown);
            }

            textView.setText(getString(R.string.text_param_gearing_custom,
                    frontGearingString, rearGearingString));
        }
    }
}