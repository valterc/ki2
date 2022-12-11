package com.valterc.ki2.fragments.update;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.valterc.ki2.BuildConfig;
import com.valterc.ki2.R;
import com.valterc.ki2.data.update.DownloadedPackageInfo;
import com.valterc.ki2.data.update.OngoingUpdateStateInfo;
import com.valterc.ki2.data.update.ReleaseInfo;
import com.valterc.ki2.data.update.UpdateStateStore;
import com.valterc.ki2.update.DownloadPackageTask;
import com.valterc.ki2.update.GetLatestReleaseInfoTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import timber.log.Timber;

public class UpdateViewModel extends ViewModel {

    private static final String ACTION_UPDATE_STATUS = BuildConfig.APPLICATION_ID + ".action.update.UPDATE_STATUS";
    private static final int TIME_MS_WAIT_BEFORE_UPDATE = 4000;

    private final MutableLiveData<ReleaseInfo> releaseInfo;
    private final MutableLiveData<UpdateStatus> updateStatus;
    private final MutableLiveData<String> errorMessage;
    private final MutableLiveData<Float> updateProgress;
    private final MutableLiveData<OngoingUpdateStateInfo> updateState;

    private final ExecutorService executor;

    private boolean cancelUpdate;

    public UpdateViewModel() {
        releaseInfo = new MutableLiveData<>();
        updateStatus = new MutableLiveData<>(UpdateStatus.START);
        errorMessage = new MutableLiveData<>();
        updateProgress = new MutableLiveData<>();
        updateState = new MutableLiveData<>();

        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<ReleaseInfo> getReleaseInfo() {
        return releaseInfo;
    }

    public void setReleaseInfo(ReleaseInfo releaseInfo) {
        this.releaseInfo.postValue(releaseInfo);

        if (releaseInfo == null) {
            this.updateStatus.postValue(UpdateStatus.START);
        } else if (!releaseInfo.getName().equals(BuildConfig.VERSION_NAME)) {
            this.updateStatus.postValue(UpdateStatus.UPDATE_AVAILABLE);
        } else {
            this.updateStatus.postValue(UpdateStatus.NO_UPDATE_AVAILABLE);
        }
    }

    public LiveData<UpdateStatus> getUpdateStatus() {
        return updateStatus;
    }

    public void setUpdateState(OngoingUpdateStateInfo ongoingUpdateStateInfo) {
        this.updateState.postValue(ongoingUpdateStateInfo);

        if (ongoingUpdateStateInfo != null) {
            this.updateStatus.postValue(UpdateStatus.UPDATE_COMPLETE);
        }
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Float> getUpdateProgress() {
        return updateProgress;
    }

    public void checkForUpdates() {
        updateStatus.postValue(UpdateStatus.CHECKING_FOR_UPDATE);
        executor.submit(() -> {
            try {
                ReleaseInfo releaseInfo = new GetLatestReleaseInfoTask().call();
                setReleaseInfo(releaseInfo);
            } catch (Exception e) {
                errorMessage.postValue(e.getMessage());
                updateStatus.postValue(UpdateStatus.ERROR);
            }
        });
    }

    public void performUpdate(@NonNull Activity context) {
        updateProgress.postValue(0f);
        updateStatus.postValue(UpdateStatus.UPDATING);
        cancelUpdate = false;

        ReleaseInfo releaseInfo = this.releaseInfo.getValue();
        executor.submit(() -> {
            try {
                Thread.sleep(TIME_MS_WAIT_BEFORE_UPDATE);
                DownloadedPackageInfo downloadedPackageInfo = new DownloadPackageTask(context, releaseInfo, updateProgress::postValue).call();
                install(context, downloadedPackageInfo);
            } catch (Exception e) {
                errorMessage.postValue(e.getMessage());
                updateStatus.postValue(UpdateStatus.ERROR);
            }
        });
    }

    public void cancelUpdate(){
        cancelUpdate = true;
    }

    public void updateFailUserRejectedPermissions(Context context) {
        Timber.e("Install error, user rejected permissions");
        errorMessage.postValue(context.getString(R.string.text_update_error_permissions));
        updateStatus.postValue(UpdateStatus.ERROR);
        UpdateStateStore.updateFailed(context);
    }

    private void install(Activity context, DownloadedPackageInfo downloadedPackageInfo) throws IOException {
        PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();

        PackageInstaller.SessionParams sessionParams = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        sessionParams.setAppPackageName(BuildConfig.APPLICATION_ID);

        int sessionId = packageInstaller.createSession(sessionParams);
        PackageInstaller.Session session = packageInstaller.openSession(sessionId);

        try {
            OutputStream outputStream = session.openWrite(downloadedPackageInfo.getReleaseInfo().getPackageName(), 0, -1);
            InputStream inputStream = new FileInputStream(downloadedPackageInfo.getPackagePath());
            byte[] buffer = new byte[1024];

            while (true) {
                int read = inputStream.read(buffer);
                if (read == -1) {
                    break;
                }

                outputStream.write(buffer, 0, read);
            }

            session.fsync(outputStream);
            outputStream.close();

            Files.deleteIfExists(Paths.get(downloadedPackageInfo.getPackagePath()));

            Intent intent = new Intent(context, context.getClass());
            intent.setAction(ACTION_UPDATE_STATUS);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(context,
                    0,
                    intent,
                    android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S ? PendingIntent.FLAG_MUTABLE : 0);
            IntentSender statusReceiver = pendingIntent.getIntentSender();

            if (cancelUpdate) {
                Timber.i("Update cancelled");
                session.abandon();
                updateStatus.postValue(UpdateStatus.START);
                return;
            }

            UpdateStateStore.willUpdate(context, BuildConfig.VERSION_NAME, downloadedPackageInfo.getReleaseInfo().getName());
            session.commit(statusReceiver);
        } catch (Exception e) {
            Timber.e(e, "Unable to install update");

            errorMessage.postValue(e.getMessage());
            updateStatus.postValue(UpdateStatus.ERROR);
            UpdateStateStore.updateFailed(context);

            if (session != null) {
                session.abandon();
            }
        }
    }

    public boolean isUpdateIntent(Intent intent) {
        return ACTION_UPDATE_STATUS.equals(intent.getAction());
    }

    public void onUpdateIntent(Context context, Intent intent) {
        if (!isUpdateIntent(intent)) {
            return;
        }

        Bundle extras = intent.getExtras();
        int status = extras.getInt(PackageInstaller.EXTRA_STATUS);
        String message = extras.getString(PackageInstaller.EXTRA_STATUS_MESSAGE);
        switch (status) {
            case PackageInstaller.STATUS_PENDING_USER_ACTION:
                Timber.w("Update pending user action");
                updateStatus.postValue(UpdateStatus.UPDATING_USER_ACTION);
                Intent confirmIntent = (Intent) extras.get(Intent.EXTRA_INTENT);
                context.startActivity(confirmIntent);
                break;

            case PackageInstaller.STATUS_SUCCESS:
                // Should never execute
                Timber.d("Install successful");
                break;

            default:
                Timber.e("Install error: %d, %s", status, message);
                errorMessage.postValue(message);
                updateStatus.postValue(UpdateStatus.ERROR);
                UpdateStateStore.updateFailed(context);
                break;
        }
    }

}