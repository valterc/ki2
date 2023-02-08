package com.valterc.ki2.fragments.devices.gearing;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.valterc.ki2.R;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.preferences.device.DevicePreferences;
import com.valterc.ki2.data.shifting.FrontTeethPattern;
import com.valterc.ki2.data.shifting.RearTeethPattern;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.services.Ki2Service;
import com.valterc.ki2.utils.ArrayUtils;

import java.util.Arrays;

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

        Spinner spinnerNumberGearsFront = view.findViewById(R.id.spinner_device_gearing_number_gears_front);
        ArrayAdapter<Integer> adapterGearCountFront = new ArrayAdapter<>(requireContext(), R.layout.view_item_gearing_spinner_text, new Integer[]{1, 2, 3});
        adapterGearCountFront.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNumberGearsFront.setAdapter(adapterGearCountFront);

        Spinner spinnerNumberGearsRear = view.findViewById(R.id.spinner_device_gearing_number_gears_rear);
        ArrayAdapter<Integer> adapterGearCountRear = new ArrayAdapter<>(requireContext(), R.layout.view_item_gearing_spinner_text, new Integer[]{9, 10, 11, 12});
        adapterGearCountRear.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNumberGearsRear.setAdapter(adapterGearCountRear);

        RecyclerView recyclerViewGearsFront = view.findViewById(R.id.recyclerview_device_gearing_front);
        DeviceGearingItemAdapter adapterGearsFront = new DeviceGearingItemAdapter(devicePreferences::setCustomGearingFront);
        recyclerViewGearsFront.setAdapter(adapterGearsFront);

        RecyclerView recyclerViewGearsRear = view.findViewById(R.id.recyclerview_device_gearing_rear);
        DeviceGearingItemAdapter adapterGearsRear = new DeviceGearingItemAdapter(devicePreferences::setCustomGearingRear);
        recyclerViewGearsRear.setAdapter(adapterGearsRear);

        int[] customGearingFront = devicePreferences.getCustomGearingFront();
        if (customGearingFront == null) {
            spinnerNumberGearsFront.setSelection(adapterGearCountFront.getPosition(FrontTeethPattern.P52_36.getGearCount()));
            adapterGearsFront.setGears(FrontTeethPattern.P52_36.getGears());
        } else {
            spinnerNumberGearsFront.setSelection(adapterGearCountFront.getPosition(customGearingFront.length));
            adapterGearsFront.setGears(customGearingFront);
        }

        int[] customGearingRear = devicePreferences.getCustomGearingRear();
        if (customGearingRear == null) {
            spinnerNumberGearsRear.setSelection(adapterGearCountRear.getPosition(RearTeethPattern.S11_P11_32.getGearCount()));
            adapterGearsRear.setGears(RearTeethPattern.S11_P11_32.getGears());
        } else {
            spinnerNumberGearsRear.setSelection(adapterGearCountRear.getPosition(customGearingRear.length));
            adapterGearsRear.setGears(customGearingRear);
        }

        spinnerNumberGearsFront.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int[] customGearingFront = devicePreferences.getCustomGearingFront();
                if (customGearingFront == null) {
                    adapterGearsFront.setGears(ArrayUtils.reverse(Arrays.copyOfRange(ArrayUtils.reverse(FrontTeethPattern.P52_36.getGears()), 0, (Integer) parent.getItemAtPosition(position))));
                } else {
                    adapterGearsFront.setGears(ArrayUtils.reverse(Arrays.copyOfRange(ArrayUtils.reverse(customGearingFront), 0, (Integer) parent.getItemAtPosition(position))));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerNumberGearsRear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int[] customGearingRear = devicePreferences.getCustomGearingRear();
                if (customGearingRear == null) {
                    adapterGearsRear.setGears(ArrayUtils.reverse(Arrays.copyOfRange(ArrayUtils.reverse(RearTeethPattern.S11_P11_32.getGears()), 0, (Integer) parent.getItemAtPosition(position))));
                } else {
                    adapterGearsRear.setGears(ArrayUtils.reverse(Arrays.copyOfRange(ArrayUtils.reverse(customGearingRear), 0, (Integer) parent.getItemAtPosition(position))));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (WindowInsetsCompat.toWindowInsetsCompat(requireView().getRootWindowInsets()).isVisible(WindowInsetsCompat.Type.ime())) {
                    requireView().clearFocus();
                    InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(requireView().getWindowToken(), 0);
                } else {
                    boolean invalid = false;
                    if (!adapterGearsFront.validateGears()) {
                        int invalidPosition = adapterGearsFront.getFirstInvalidGearIndex();
                        recyclerViewGearsFront.scrollToPosition(invalidPosition);
                        DeviceGearingItemViewHolder holder = (DeviceGearingItemViewHolder) recyclerViewGearsFront.findViewHolderForAdapterPosition(invalidPosition);
                        if (holder != null) {
                            holder.getEditTextGear().requestFocus();
                        }
                        invalid = true;
                    }
                    if (!adapterGearsRear.validateGears()) {
                        int invalidPosition = adapterGearsRear.getFirstInvalidGearIndex();
                        recyclerViewGearsRear.scrollToPosition(invalidPosition);
                        DeviceGearingItemViewHolder holder = (DeviceGearingItemViewHolder) recyclerViewGearsRear.findViewHolderForAdapterPosition(invalidPosition);
                        if (holder != null) {
                            holder.getEditTextGear().requestFocus();
                        }
                        invalid = true;
                    }

                    if (invalid) {
                        new AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle)
                                .setTitle("Invalid gears")
                                .setMessage("Some of the custom gears are invalid.\n\nThe latest changes might not be saved if you exit now.")
                                .setPositiveButton("Exit", (dialog, whichButton) -> {
                                    setEnabled(false);
                                    requireActivity().onBackPressed();
                                })
                                .setNegativeButton("Cancel", (dialog, whichButton) -> {})
                                .show();
                    } else {
                        setEnabled(false);
                        requireActivity().onBackPressed();
                    }
                }
            }
        });
    }
}