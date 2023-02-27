package com.valterc.ki2.fragments.settings.overlay;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentResultListener;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.valterc.ki2.R;

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
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }
}