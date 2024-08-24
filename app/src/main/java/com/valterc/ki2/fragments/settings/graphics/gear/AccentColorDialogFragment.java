package com.valterc.ki2.fragments.settings.graphics.gear;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.valterc.ki2.R;

public class AccentColorDialogFragment extends DialogFragment {

    private static final String PREFERENCE_KEY = "PreferenceKey";

    public static final String RESULT_VALUE = "ResultValue";

    public static AccentColorDialogFragment newInstance(String preferenceKey) {
        AccentColorDialogFragment fragment = new AccentColorDialogFragment();
        final Bundle b = new Bundle(1);
        b.putString(PREFERENCE_KEY, preferenceKey);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_gear_color, container, false);

        RecyclerView recyclerViewOptions = view.findViewById(R.id.recyclerview_gear_color_options);
        AccentColorAdapter adapter = new AccentColorAdapter(requireContext(), value -> {
            Bundle bundle = new Bundle(1);
            bundle.putString(RESULT_VALUE, value);
            getParentFragmentManager().setFragmentResult(requireArguments().getString(PREFERENCE_KEY), bundle);
            dismiss();
        });
        recyclerViewOptions.setAdapter(adapter);
        recyclerViewOptions.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        String currentValue = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString(requireArguments().getString(PREFERENCE_KEY), null);
        recyclerViewOptions.smoothScrollToPosition(adapter.getItemIndex(currentValue));

        Button buttonCancel = view.findViewById(R.id.button_gear_color_cancel);
        buttonCancel.setOnClickListener(v -> dismiss());

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
