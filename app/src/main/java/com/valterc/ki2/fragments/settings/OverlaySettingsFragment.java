package com.valterc.ki2.fragments.settings;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.valterc.ki2.R;

import java.util.Objects;
import java.util.Set;

public class OverlaySettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_overlay, rootKey);

        MultiSelectListPreference preferenceOverlayTriggers = findPreference(getString(R.string.preference_overlay_triggers));
        Objects.requireNonNull(preferenceOverlayTriggers)
                .setOnPreferenceChangeListener((preference, newValue) -> {
                    if (newValue instanceof Set<?> && ((Set<?>) newValue).isEmpty()) {
                        Toast.makeText(getContext(), "At least one trigger must be selected", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    return true;
                });
    }

    @Override
    public void onDisplayPreferenceDialog(@NonNull Preference preference) {
        super.onDisplayPreferenceDialog(preference);
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final RecyclerView recyclerView = getListView();
        final Resources resources = getResources();
        float paddingBottom = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, resources.getDisplayMetrics());
        recyclerView.setPadding(0, 0, 0, (int) paddingBottom);
    }

}