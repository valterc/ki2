package com.valterc.ki2.fragments.settings.overlay;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.View;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import androidx.recyclerview.widget.RecyclerView;

import com.valterc.ki2.R;
import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.fragments.settings.overlay.opacity.OpacityPreference;
import com.valterc.ki2.fragments.settings.overlay.opacity.OverlayOpacityDialogFragment;
import com.valterc.ki2.fragments.settings.overlay.position.OverlayPositionDialogFragment;
import com.valterc.ki2.fragments.settings.overlay.position.PositionPreference;
import com.valterc.ki2.fragments.settings.overlay.theme.OverlayThemeDialogFragment;

import java.util.Objects;

public class OverlaySettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_overlay, rootKey);

        SwitchPreference preferenceOverlayEnabled = findPreference(getString(R.string.preference_overlay_enabled));

        var startForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (Settings.canDrawOverlays(requireContext())) {
                Objects.requireNonNull(preferenceOverlayEnabled).setChecked(true);
            }
        });

        Objects.requireNonNull(preferenceOverlayEnabled).setOnPreferenceChangeListener((preference, newValue) -> {
            if (Objects.equals(newValue, Boolean.TRUE)) {
                if (!Settings.canDrawOverlays(requireContext())) {
                    startForResult.launch(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
                    return false;
                }
            }

            return true;
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final RecyclerView recyclerView = getListView();
        final Resources resources = getResources();
        float paddingBottom = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, resources.getDisplayMetrics());
        recyclerView.setPadding(0, 0, 0, (int) paddingBottom);
    }

    @Override
    public void onDisplayPreferenceDialog(@NonNull Preference preference) {
        if (Objects.equals(preference.getKey(), getString(R.string.preference_overlay_theme))) {
            getParentFragmentManager().setFragmentResultListener(getString(R.string.preference_overlay_theme), getViewLifecycleOwner(), (requestKey, result) ->
                    ((ListPreference) preference).setValue(result.getString(OverlayThemeDialogFragment.RESULT_VALUE)));
            OverlayThemeDialogFragment.newInstance(preference.getKey()).show(getParentFragmentManager(), null);
        } else if (Objects.equals(preference.getKey(), getString(R.string.preference_secondary_overlay_theme))) {
            getParentFragmentManager().setFragmentResultListener(getString(R.string.preference_secondary_overlay_theme), getViewLifecycleOwner(), (requestKey, result) ->
                    ((ListPreference) preference).setValue(result.getString(OverlayThemeDialogFragment.RESULT_VALUE)));
            OverlayThemeDialogFragment.newInstance(preference.getKey()).show(getParentFragmentManager(), null);
        } else if (Objects.equals(preference.getKey(), getString(R.string.preference_overlay_opacity))) {
            handleOverlayOpacityDialog(preference);
        } else if (Objects.equals(preference.getKey(), getString(R.string.preference_secondary_overlay_opacity))) {
            handleSecondaryOverlayOpacityDialog(preference);
        } else if (preference instanceof PositionPreference positionPreference) {
            getParentFragmentManager().setFragmentResultListener(OverlayPositionDialogFragment.DEFAULT_REQUEST_KEY, getViewLifecycleOwner(), (requestKey, result) ->
                    positionPreference.setValue(result.getInt(OverlayPositionDialogFragment.RESULT_POSITION_X), result.getInt(OverlayPositionDialogFragment.RESULT_POSITION_Y)));
            DialogFragment fragment = OverlayPositionDialogFragment.newInstance(OverlayPositionDialogFragment.DEFAULT_REQUEST_KEY, positionPreference.getOverlayTheme(), positionPreference.getPositionX(), positionPreference.getPositionY());
            fragment.show(getParentFragmentManager(), null);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    private void handleOverlayOpacityDialog(@NonNull Preference preference) {
        getParentFragmentManager().setFragmentResultListener(getString(R.string.preference_overlay_opacity), getViewLifecycleOwner(), (requestKey, result) ->
                ((OpacityPreference) preference).setValue(result.getFloat(OverlayOpacityDialogFragment.RESULT_VALUE)));

        PreferencesView preferencesView = new PreferencesView(getContext());
        OverlayOpacityDialogFragment.newInstance(preference.getKey(), preferencesView.getOverlayTheme(requireContext()), preferencesView.getOverlayOpacity(requireContext())).show(getParentFragmentManager(), null);
    }

    private void handleSecondaryOverlayOpacityDialog(@NonNull Preference preference) {
        getParentFragmentManager().setFragmentResultListener(getString(R.string.preference_secondary_overlay_opacity), getViewLifecycleOwner(), (requestKey, result) ->
                ((OpacityPreference) preference).setValue(result.getFloat(OverlayOpacityDialogFragment.RESULT_VALUE)));

        PreferencesView preferencesView = new PreferencesView(getContext());
        OverlayOpacityDialogFragment.newInstance(preference.getKey(), preferencesView.getSecondaryOverlayTheme(requireContext()), preferencesView.getSecondaryOverlayOpacity(requireContext())).show(getParentFragmentManager(), null);
    }
}