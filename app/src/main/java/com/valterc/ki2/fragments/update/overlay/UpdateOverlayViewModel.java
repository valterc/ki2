package com.valterc.ki2.fragments.update.overlay;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.valterc.ki2.BuildConfig;
import com.valterc.ki2.data.update.ReleaseInfo;
import com.valterc.ki2.update.GetLatestReleaseInfoTask;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import timber.log.Timber;

public class UpdateOverlayViewModel extends ViewModel {

    private final MutableLiveData<UpdateCheckStatus> updateCheckStatus;
    private final MutableLiveData<ReleaseInfo> releaseInfo;
    private final ExecutorService executor;

    public UpdateOverlayViewModel() {
        updateCheckStatus = new MutableLiveData<>(UpdateCheckStatus.NEW);
        releaseInfo = new MutableLiveData<>();
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<UpdateCheckStatus> getUpdateCheckStatus() {
        return updateCheckStatus;
    }

    public LiveData<ReleaseInfo> getReleaseInfo() {
        return releaseInfo;
    }

    public void checkForUpdates() {
        updateCheckStatus.postValue(UpdateCheckStatus.CHECKING);
        executor.submit(() -> {
            try {
                ReleaseInfo releaseInfo = new GetLatestReleaseInfoTask().call();
                this.releaseInfo.postValue(releaseInfo);
                if (!releaseInfo.getName().equals(BuildConfig.VERSION_NAME)) {
                    updateCheckStatus.postValue(UpdateCheckStatus.UPDATE_AVAILABLE);
                } else {
                    updateCheckStatus.postValue(UpdateCheckStatus.NO_UPDATE_AVAILABLE);
                }
            } catch (Exception e) {
                Timber.e(e, "Unable to check for updates");
                updateCheckStatus.postValue(UpdateCheckStatus.ERROR);
            }
        });
    }

}