package com.valterc.ki2.fragments.settings.overlay.position;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

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
import com.valterc.ki2.karoo.extension.Ki2ExtensionContext;
import com.valterc.ki2.karoo.overlay.view.IOverlayView;
import com.valterc.ki2.karoo.overlay.view.builder.OverlayViewBuilderEntry;
import com.valterc.ki2.karoo.overlay.view.builder.OverlayViewBuilderRegistry;

import io.hammerhead.sdk.v0.SdkContext;

public class OverlayPositionDialogFragment extends DialogFragment {

    private static final String REQUEST_KEY = "RequestKey";

    private static final String PARAMETER_THEME = "ParameterTheme";
    private static final String PARAMETER_POSITION_X = "ParameterPositionX";
    private static final String PARAMETER_POSITION_Y = "ParameterPositionY";

    public static final String DEFAULT_REQUEST_KEY = "OverlayPosition";
    public static final String RESULT_POSITION_X = "ResultPositionX";
    public static final String RESULT_POSITION_Y = "ResultPositionY";


    public static OverlayPositionDialogFragment newInstance(String requestKey, String theme, int positionX, int positionY) {
        OverlayPositionDialogFragment fragment = new OverlayPositionDialogFragment();
        final Bundle b = new Bundle(4);
        b.putString(REQUEST_KEY, requestKey);
        b.putString(PARAMETER_THEME, theme);
        b.putInt(PARAMETER_POSITION_X, positionX);
        b.putInt(PARAMETER_POSITION_Y, positionY);
        fragment.setArguments(b);
        return fragment;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_overlay_position, container, false);

        final String theme = requireArguments().getString(PARAMETER_THEME);
        final int positionX = requireArguments().getInt(PARAMETER_POSITION_X);
        final int positionY = requireArguments().getInt(PARAMETER_POSITION_Y);

        PreferencesView preferencesView = new PreferencesView(requireContext());

        RelativeLayout relativeLayout = view.findViewById(R.id.relativelayout_overlay_position);
        Ki2ExtensionContext ki2Context = new Ki2ExtensionContext(requireContext());

        OverlayViewBuilderEntry entry = OverlayViewBuilderRegistry.getBuilder(theme);
        assert entry != null;

        View viewOverlay = inflater.inflate(entry.getLayoutId(), relativeLayout, false);

        DeviceId deviceId = new DeviceId(67726, 1, 5);
        DevicePreferencesView devicePreferencesView = new DevicePreferencesView(requireContext(), deviceId);
        ConnectionInfo connectionInfo = new ConnectionInfo(ConnectionStatus.ESTABLISHED);
        BatteryInfo batteryInfo = new BatteryInfo(80);
        ShiftingInfo shiftingInfo = new ShiftingInfo(BuzzerType.DEFAULT, 2, 2, 5, 11, FrontTeethPattern.P50_34, RearTeethPattern.S11_P11_30, ShiftingMode.SYNCHRONIZED_SHIFT_MODE_2);

        IOverlayView overlayView = entry.createOverlayView(ki2Context, preferencesView, viewOverlay);
        overlayView.updateView(connectionInfo, devicePreferencesView, batteryInfo, shiftingInfo);
        relativeLayout.addView(viewOverlay);

        ScaledPositionManager positionManager = new ScaledPositionManager(relativeLayout);
        positionManager.applyPosition(positionX, positionY, viewOverlay);

        relativeLayout.setClickable(true);
        relativeLayout.setOnTouchListener((v, event) -> {
            positionManager.applyPositionCenter((int)event.getX(), (int)event.getY(), viewOverlay);
            return false;
        });

        Button buttonDefault = view.findViewById(R.id.button_overlay_position_default);
        buttonDefault.setOnClickListener(v -> positionManager.applyPosition(entry.getDefaultPositionX(), entry.getDefaultPositionY(), viewOverlay));

        Button buttonCancel = view.findViewById(R.id.button_overlay_position_cancel);
        buttonCancel.setOnClickListener(v -> dismiss());

        Button buttonOk = view.findViewById(R.id.button_overlay_position_ok);
        buttonOk.setOnClickListener(v -> {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)viewOverlay.getLayoutParams();
            Bundle bundle = new Bundle(2);
            bundle.putInt(RESULT_POSITION_X, layoutParams.leftMargin);
            bundle.putInt(RESULT_POSITION_Y, layoutParams.topMargin);
            getParentFragmentManager().setFragmentResult(requireArguments().getString(REQUEST_KEY), bundle);
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
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }
}
