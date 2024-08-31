package com.valterc.ki2.fragments.settings.overlay.opacity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.slider.BasicLabelFormatter;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;
import com.valterc.ki2.R;
import com.valterc.ki2.data.connection.ConnectionInfo;
import com.valterc.ki2.data.connection.ConnectionStatus;
import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.data.preferences.device.DevicePreferencesView;
import com.valterc.ki2.data.shifting.BuzzerType;
import com.valterc.ki2.data.shifting.FrontTeethPattern;
import com.valterc.ki2.data.shifting.RearTeethPattern;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.data.shifting.ShiftingMode;
import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.karoo.overlay.view.IOverlayView;
import com.valterc.ki2.karoo.overlay.view.builder.OverlayViewBuilderEntry;
import com.valterc.ki2.karoo.overlay.view.builder.OverlayViewBuilderRegistry;

import java.util.Locale;

import io.hammerhead.sdk.v0.SdkContext;

public class OverlayOpacityDialogFragment extends DialogFragment {


    private static final String PREFERENCE_KEY = "PreferenceKey";
    private static final String PARAMETER_THEME = "ParameterTheme";
    private static final String PARAMETER_OPACITY = "ParameterOpacity";

    public static final String RESULT_VALUE = "ResultValue";

    public static OverlayOpacityDialogFragment newInstance(String preferenceKey, String overlayTheme, float overlayOpacity) {
        OverlayOpacityDialogFragment fragment = new OverlayOpacityDialogFragment();
        final Bundle b = new Bundle(3);
        b.putString(PREFERENCE_KEY, preferenceKey);
        b.putString(PARAMETER_THEME, overlayTheme);
        b.putFloat(PARAMETER_OPACITY, overlayOpacity);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_overlay_opacity, container, false);

        PreferencesView preferencesView = new PreferencesView(requireContext());

        LinearLayout linearLayoutContainer = view.findViewById(R.id.viewstub_overlay_opacity_view_container);
        Ki2Context ki2Context = new Ki2Context(SdkContext.buildSdkContext(requireContext()));
        OverlayViewBuilderEntry entry = OverlayViewBuilderRegistry.getBuilder(requireArguments().getString(PARAMETER_THEME));
        View viewOverlay = inflater.inflate(entry.getLayoutId(), linearLayoutContainer, false);

        DeviceId deviceId = new DeviceId(67726, 1, 5);
        DevicePreferencesView devicePreferencesView = new DevicePreferencesView(requireContext(), deviceId);
        ConnectionInfo connectionInfo = new ConnectionInfo(ConnectionStatus.ESTABLISHED);
        BatteryInfo batteryInfo = new BatteryInfo(80);
        ShiftingInfo shiftingInfo = new ShiftingInfo(BuzzerType.DEFAULT, 2, 2, 5, 11, FrontTeethPattern.P50_34, RearTeethPattern.S11_P11_30, ShiftingMode.SYNCHRONIZED_SHIFT_MODE_2);

        IOverlayView overlayView = entry.createOverlayView(ki2Context, preferencesView, viewOverlay);
        overlayView.updateView(connectionInfo, devicePreferencesView, batteryInfo, shiftingInfo);
        overlayView.setAlpha(requireArguments().getFloat(PARAMETER_OPACITY));
        viewOverlay.setElevation(0);
        linearLayoutContainer.addView(viewOverlay);

        Slider slider = view.findViewById(R.id.slider_overlay_opacity);
        slider.setValue(requireArguments().getFloat(PARAMETER_OPACITY));
        slider.setLabelFormatter(value -> String.format(Locale.getDefault(), "%.0f", value * 100) + "%");
        slider.addOnChangeListener((s, value, fromUser) -> overlayView.setAlpha(value));

        Button buttonCancel = view.findViewById(R.id.button_overlay_opacity_cancel);
        buttonCancel.setOnClickListener(v -> dismiss());

        Button buttonOk = view.findViewById(R.id.button_overlay_opacity_ok);
        buttonOk.setOnClickListener(v -> {
            Bundle bundle = new Bundle(1);
            bundle.putFloat(RESULT_VALUE, slider.getValue());
            getParentFragmentManager().setFragmentResult(requireArguments().getString(PREFERENCE_KEY), bundle);
            dismiss();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }
}
