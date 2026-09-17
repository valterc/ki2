package com.valterc.ki2.fragments.settings;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.valterc.ki2.R;
import com.valterc.ki2.data.update.UpdateStateStore;

public class UpdateSettingsFragment extends PreferenceFragmentCompat {

    private final SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener =
            (sharedPreferences, key) -> {
                if (getString(R.string.preference_preview_updates).equals(key)) {
                    UpdateStateStore.resetUpdateCheckState(requireContext());
                }
            };

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_update, rootKey);
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
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        if (sharedPreferences != null) {
            sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        if (sharedPreferences != null) {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
        }
    }

}
