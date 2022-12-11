package com.valterc.ki2.fragments.update.overlay;

import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.valterc.ki2.R;
import com.valterc.ki2.activities.update.UpdateActivity;
import com.valterc.ki2.data.update.ReleaseInfo;
import com.valterc.ki2.data.update.UpdateStateStore;

import java.util.Arrays;
import java.util.Collection;

public class UpdateOverlayFragment extends Fragment {

    private static final String STATE_KEY_SHOULD_REMOVE = "ShouldRemove";

    public static UpdateOverlayFragment newInstance() {
        return new UpdateOverlayFragment();
    }

    Handler handler = new Handler(Looper.getMainLooper());
    private UpdateOverlayViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_update_overlay, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(UpdateOverlayViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View linearLayoutCheckingForUpdates = view.findViewById(R.id.linearlayout_update_overlay_checking_for_updates);
        View linearLayoutUpdateAvailable = view.findViewById(R.id.linearlayout_update_overlay_update_available);
        View linearLayoutNoUpdateAvailable = view.findViewById(R.id.linearlayout_update_overlay_no_update_available);
        View linearLayoutError = view.findViewById(R.id.linearlayout_update_overlay_error);

        Collection<View> allViews = Arrays.asList(
                linearLayoutCheckingForUpdates,
                linearLayoutUpdateAvailable,
                linearLayoutNoUpdateAvailable,
                linearLayoutError);

        Button buttonUpdate = view.findViewById(R.id.button_update_overlay_update);
        buttonUpdate.setOnClickListener((event) -> {
            removeSelf();
            startActivity(new Intent(requireContext(), UpdateActivity.class));
        });

        viewModel.getUpdateCheckStatus().observe(getViewLifecycleOwner(), updateCheckStatus -> {
            allViews.forEach(v -> v.setVisibility(View.GONE));

            switch (updateCheckStatus) {
                case NEW:
                case CHECKING:
                    linearLayoutCheckingForUpdates.setVisibility(View.VISIBLE);
                    break;

                case UPDATE_AVAILABLE:
                    linearLayoutUpdateAvailable.setVisibility(View.VISIBLE);
                    ReleaseInfo releaseInfo = viewModel.getReleaseInfo().getValue();
                    if (releaseInfo != null) {
                        UpdateStateStore.checkedForUpdates(requireContext(), true, releaseInfo.getName());
                    }
                    break;

                case NO_UPDATE_AVAILABLE:
                    linearLayoutNoUpdateAvailable.setVisibility(View.VISIBLE);
                    handler.postDelayed(this::removeSelf, 5000);
                    UpdateStateStore.checkedForUpdates(requireContext(), false, null);
                    break;

                case ERROR:
                    linearLayoutError.setVisibility(View.VISIBLE);
                    handler.postDelayed(this::removeSelf, 5000);
                    break;
            }
        });

        if (savedInstanceState != null &&
                savedInstanceState.getBoolean(STATE_KEY_SHOULD_REMOVE, false)) {
            handler.postDelayed(this::removeSelf, 250);
        } else {
            handler.postDelayed(viewModel::checkForUpdates, 500);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (viewModel != null) {
            UpdateCheckStatus updateCheckStatus = viewModel.getUpdateCheckStatus().getValue();
            if (updateCheckStatus == UpdateCheckStatus.NO_UPDATE_AVAILABLE || updateCheckStatus == UpdateCheckStatus.ERROR) {
                outState.putBoolean(STATE_KEY_SHOULD_REMOVE, true);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            UpdateCheckStatus updateCheckStatus = viewModel.getUpdateCheckStatus().getValue();
            if (updateCheckStatus == UpdateCheckStatus.NO_UPDATE_AVAILABLE || updateCheckStatus == UpdateCheckStatus.ERROR) {
                handler.postDelayed(this::removeSelf, 250);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
    }

    private void removeSelf() {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.anim_enter_top, R.anim.anim_exit_top, R.anim.anim_enter_top, R.anim.anim_exit_top);
        transaction.remove(this);
        transaction.commitNow();
    }
}