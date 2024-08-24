package com.valterc.ki2.fragments.settings.graphics;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.valterc.ki2.R;
import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.fragments.settings.graphics.gear.GearColorDialogFragment;
import com.valterc.ki2.fragments.settings.graphics.gear.GearColorPreference;
import com.valterc.ki2.fragments.settings.overlay.opacity.OpacityPreference;
import com.valterc.ki2.fragments.settings.overlay.opacity.OverlayOpacityDialogFragment;
import com.valterc.ki2.fragments.settings.overlay.position.OverlayPositionDialogFragment;
import com.valterc.ki2.fragments.settings.overlay.position.PositionPreference;
import com.valterc.ki2.fragments.settings.overlay.theme.OverlayThemeDialogFragment;

import java.util.Objects;

public class GraphicsSettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_graphics, rootKey);
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
        if (Objects.equals(preference.getKey(), getString(R.string.preference_gear_color))) {
            handleGearColorDialog((GearColorPreference) preference);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    private void handleGearColorDialog(@NonNull GearColorPreference preference) {
        getParentFragmentManager().setFragmentResultListener(getString(R.string.preference_gear_color), getViewLifecycleOwner(), (requestKey, result) ->
                preference.setValue(result.getString(GearColorDialogFragment.RESULT_VALUE)));
        GearColorDialogFragment.newInstance(preference.getKey()).show(getParentFragmentManager(), null);
    }

}