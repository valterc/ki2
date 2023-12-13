package com.valterc.ki2.fragments.settings;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.valterc.ki2.R;

import java.util.Objects;

public class GeneralSettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_general, rootKey);
        Preference preferenceFITRecording = findPreference(getString(R.string.preference_ant_recording));
        Objects.requireNonNull(preferenceFITRecording).setOnPreferenceChangeListener((preference, newValue) -> {
            if (Objects.equals(newValue, Boolean.TRUE)) {
                new AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle)
                        .setTitle(R.string.text_ant_recording)
                        .setMessage(getString(R.string.text_warning_ant_recording))
                        .setPositiveButton(android.R.string.ok, (dialog, whichButton) -> dialog.dismiss())
                        .show();
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

}