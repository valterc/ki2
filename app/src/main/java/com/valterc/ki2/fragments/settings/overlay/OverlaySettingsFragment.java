package com.valterc.ki2.fragments.settings.overlay;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.valterc.ki2.R;
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
                    ((ListPreference)preference).setValue(result.getString(OverlayThemeDialogFragment.RESULT_VALUE)));
            OverlayThemeDialogFragment.newInstance(preference.getKey()).show(getParentFragmentManager(), null);
        } else if (Objects.equals(preference.getKey(), getString(R.string.preference_overlay_opacity))) {
            getParentFragmentManager().setFragmentResultListener(getString(R.string.preference_overlay_opacity), getViewLifecycleOwner(), (requestKey, result) ->
                    ((OpacityPreference)preference).setValue(result.getFloat(OverlayOpacityDialogFragment.RESULT_VALUE)));
            OverlayOpacityDialogFragment.newInstance(preference.getKey()).show(getParentFragmentManager(), null);
        } else if (preference instanceof PositionPreference) {
            getParentFragmentManager().setFragmentResultListener(OverlayPositionDialogFragment.DEFAULT_REQUEST_KEY, getViewLifecycleOwner(), (requestKey, result) ->
                    ((PositionPreference) preference).setValue(result.getInt(OverlayPositionDialogFragment.RESULT_POSITION_X), result.getInt(OverlayPositionDialogFragment.RESULT_POSITION_Y)));
            OverlayPositionDialogFragment.newInstance(OverlayPositionDialogFragment.DEFAULT_REQUEST_KEY).show(getParentFragmentManager(), null);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }
}