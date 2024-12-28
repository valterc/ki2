package com.valterc.ki2.fragments.update;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.valterc.ki2.BuildConfig;
import com.valterc.ki2.R;
import com.valterc.ki2.data.update.OngoingUpdateStateInfo;
import com.valterc.ki2.data.update.ReleaseInfo;
import com.valterc.ki2.data.update.UpdateStateStore;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;

public class UpdateFragment extends Fragment {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
                    .withZone(ZoneId.from(ZoneOffset.UTC));

    private UpdateViewModel viewModel;

    public static UpdateFragment newInstance() {
        return new UpdateFragment();
    }

    public static UpdateFragment newInstance(ReleaseInfo releaseInfo) {
        UpdateFragment updateFragment = new UpdateFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(ReleaseInfo.class.getSimpleName(), releaseInfo);
        updateFragment.setArguments(bundle);

        return updateFragment;
    }

    public static UpdateFragment newInstance(OngoingUpdateStateInfo ongoingUpdateStateInfo) {
        UpdateFragment updateFragment = new UpdateFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(OngoingUpdateStateInfo.class.getSimpleName(), ongoingUpdateStateInfo);
        updateFragment.setArguments(bundle);

        return updateFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(UpdateViewModel.class);

        Bundle bundle = getArguments();
        if (bundle != null) {
            ReleaseInfo releaseInfo = bundle.getParcelable(ReleaseInfo.class.getSimpleName());
            if (releaseInfo != null) {
                viewModel.setReleaseInfo(releaseInfo);
            }

            OngoingUpdateStateInfo ongoingUpdateStateInfo = bundle.getParcelable(OngoingUpdateStateInfo.class.getSimpleName());
            if (ongoingUpdateStateInfo != null) {
                viewModel.setUpdateState(ongoingUpdateStateInfo);
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_update, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (viewModel.getUpdateStatus().getValue() == UpdateStatus.UPDATING_USER_ACTION &&
                UpdateStateStore.isUpdateOngoing(requireContext()) &&
                !requireActivity().getPackageManager().canRequestPackageInstalls()) {
            viewModel.updateFailUserRejectedPermissions(requireContext());
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View linearLayoutCheckForUpdates = view.findViewById(R.id.linearlayout_update_check_for_updates);
        View linearLayoutCheckingForUpdates = view.findViewById(R.id.linearlayout_update_checking_for_updates);
        View linearLayoutUpdateNotAvailable = view.findViewById(R.id.linearlayout_update_not_available);
        View linearLayoutError = view.findViewById(R.id.linearlayout_update_error);
        View linearLayoutUpdating = view.findViewById(R.id.linearlayout_update_updating);
        View linearLayoutUpdateAvailable = view.findViewById(R.id.linearlayout_update_available);
        View linearLayoutUpdateNewVersion = view.findViewById(R.id.linearlayout_update_new_version);
        View linearlayoutUpdateComplete = view.findViewById(R.id.linearlayout_update_complete);

        Collection<View> allViews = Arrays.asList(
                linearLayoutCheckForUpdates,
                linearLayoutCheckingForUpdates,
                linearLayoutUpdateNotAvailable,
                linearLayoutError,
                linearLayoutUpdating,
                linearLayoutUpdateAvailable,
                linearLayoutUpdateNewVersion,
                linearlayoutUpdateComplete);

        TextView textViewCurrentVersion = view.findViewById(R.id.textview_update_current_version);
        textViewCurrentVersion.setText(BuildConfig.VERSION_NAME);

        TextView textViewError = view.findViewById(R.id.textview_update_error);
        TextView textViewNewVersion = view.findViewById(R.id.textview_update_new_version);
        TextView textViewAvailableNewVersion = view.findViewById(R.id.textview_update_available_new_version);
        TextView textViewReleaseDate = view.findViewById(R.id.textview_update_release_date);
        TextView textViewReleaseDetails = view.findViewById(R.id.textview_update_release_details);
        TextView textViewProgress = view.findViewById(R.id.textview_update_progress);

        LinearProgressIndicator linearProgressIndicatorProgress = view.findViewById(R.id.linearprogressindicator_update_progress);

        Button buttonCheckForUpdates = view.findViewById(R.id.button_update_check_for_updates);
        Button buttonUpdate = view.findViewById(R.id.button_update);
        buttonCheckForUpdates.setOnClickListener((event) -> viewModel.checkForUpdates());
        buttonUpdate.setOnClickListener((event) -> {
            if (UpdateStateStore.isFirstUpdate(requireContext())) {
                new UpdateTutorialDialog(requireContext(), null, () -> viewModel.performUpdate(requireActivity())).show();
            } else {
                viewModel.performUpdate(requireActivity());
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), textViewError::setText);

        viewModel.getReleaseInfo().observe(getViewLifecycleOwner(), releaseInfo -> {
            if (releaseInfo != null) {
                textViewNewVersion.setText(releaseInfo.getName());
                textViewAvailableNewVersion.setText(releaseInfo.getName());
                textViewReleaseDate.setText(DATE_TIME_FORMATTER.format(releaseInfo.getPublishedAt()));
                textViewReleaseDetails.setText(formatMarkdown(releaseInfo.getDescription()));
            }
        });

        viewModel.getUpdateStatus().observe(getViewLifecycleOwner(), updateStatus -> {
            allViews.forEach(v -> v.setVisibility(View.GONE));

            switch (updateStatus) {
                case START:
                    linearLayoutCheckForUpdates.setVisibility(View.VISIBLE);
                    break;

                case CHECKING_FOR_UPDATE:
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
                    linearLayoutUpdateNotAvailable.setVisibility(View.VISIBLE);
                    UpdateStateStore.checkedForUpdates(requireContext(), false, null);
                    break;

                case UPDATING:
                case UPDATING_USER_ACTION:
                    linearLayoutUpdateNewVersion.setVisibility(View.VISIBLE);
                    linearLayoutUpdating.setVisibility(View.VISIBLE);
                    break;

                case ERROR:
                    linearLayoutError.setVisibility(View.VISIBLE);
                    linearLayoutCheckForUpdates.setVisibility(View.VISIBLE);
                    break;

                case UPDATE_COMPLETE:
                    linearlayoutUpdateComplete.setVisibility(View.VISIBLE);
                    break;
            }

        });

        viewModel.getUpdateProgress().observe(getViewLifecycleOwner(), progress -> {
            textViewProgress.setText(getString(R.string.text_param_percentage, (int) (progress * 100)));
            linearProgressIndicatorProgress.setProgress((int) (progress * 100));
        });
    }

    private SpannableStringBuilder formatMarkdown(String original) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

        int startLineIndex = 0;
        while (startLineIndex < original.length()) {
            int endOfLineIndex = original.indexOf("\n", startLineIndex);

            if (endOfLineIndex == -1) {
                endOfLineIndex = original.length();
            } else {
                endOfLineIndex += 1;
            }

            String line = original.substring(startLineIndex, endOfLineIndex);

            if (line.startsWith("### ")) {
                int from = spannableStringBuilder.length();
                spannableStringBuilder.append(line.substring(4));
                spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), from, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                spannableStringBuilder.append("\n");
            } else if (line.startsWith("## ")) {
                int from = spannableStringBuilder.length();
                spannableStringBuilder.append(line.substring(3));
                spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), from, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                spannableStringBuilder.append("\n");
            } else if (line.startsWith("# ")) {
                int from = spannableStringBuilder.length();
                spannableStringBuilder.append(line.substring(2));
                spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), from, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                spannableStringBuilder.append("\n");
            } else if (line.startsWith("* ")) {
                spannableStringBuilder.append('-');
                spannableStringBuilder.append(' ');
                spannableStringBuilder.append(line.substring(2));
            } else {
                spannableStringBuilder.append(line);
            }

            startLineIndex = endOfLineIndex;
        }

        return spannableStringBuilder;
    }

    @Override
    public void onPause() {
        super.onPause();
        UpdateStatus updateStatus = viewModel.getUpdateStatus().getValue();
        if (updateStatus == UpdateStatus.UPDATING) {
            viewModel.cancelUpdate();
        }
    }

    public boolean isUpdateIntent(Intent intent) {
        return viewModel.isUpdateIntent(intent);
    }

    public void onUpdateIntent(Intent intent) {
        viewModel.onUpdateIntent(getContext(), intent);
    }

    public void checkForUpdates() {
        if (viewModel != null &&
                (viewModel.getUpdateStatus().getValue() == null ||
                        viewModel.getUpdateStatus().getValue() == UpdateStatus.START)) {
            viewModel.checkForUpdates();
        }
    }

}