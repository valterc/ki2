package com.valterc.ki2.update.background;

import android.content.Context;

import com.valterc.ki2.BuildConfig;
import com.valterc.ki2.data.update.ReleaseInfo;
import com.valterc.ki2.data.update.UpdateStateStore;
import com.valterc.ki2.update.GetLatestReleaseInfoTask;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class BackgroundUpdateChecker {

    private static final int MAX_CHECK_ATTEMPTS = 10;
    private static final int TIME_S_WAIT_STARTUP_BEFORE_CHECK = 10;
    private static final int TIME_S_WAIT_BEFORE_CHECK = 5;

    private final Context context;
    private final IUpdateCheckerListener listener;
    private final ScheduledExecutorService executor;
    private int checkAttempts;

    public BackgroundUpdateChecker(Context context, IUpdateCheckerListener updateCheckerListener) {
        this.context = context;
        this.listener = updateCheckerListener;
        this.executor = Executors.newSingleThreadScheduledExecutor();
        this.executor.schedule(this::checkForUpdates, TIME_S_WAIT_STARTUP_BEFORE_CHECK, TimeUnit.SECONDS);
    }

    private void checkForUpdates() {
        if (UpdateStateStore.shouldAutomaticallyCheckForUpdatesInBackground(context)) {
            GetLatestReleaseInfoTask getLatestReleaseInfoTask = new GetLatestReleaseInfoTask();
            try {
                Timber.d("Attempting to check for updates");
                ReleaseInfo releaseInfo = getLatestReleaseInfoTask.call();

                boolean updateAvailable = !releaseInfo.getName().equals(BuildConfig.VERSION_NAME);
                UpdateStateStore.checkedForUpdates(context, updateAvailable, releaseInfo.getName());

                if (updateAvailable) {
                    Timber.i("Ki2 update available, current version: %s, latest version: %s", BuildConfig.VERSION_NAME, releaseInfo.getName());
                    if (listener != null) {
                        listener.onNewUpdateAvailable(releaseInfo);
                    }
                } else {
                    Timber.i("Ki2 no update available, latest version: %s", releaseInfo.getName());
                }
            } catch (Exception e) {
                Timber.w(e, "Unable to check for updates");

                if (++checkAttempts < MAX_CHECK_ATTEMPTS) {
                    this.executor.schedule(this::checkForUpdates, TIME_S_WAIT_BEFORE_CHECK + (int) (Math.random() * TIME_S_WAIT_BEFORE_CHECK), TimeUnit.SECONDS);
                }
            }
        }
    }

    public void tryCheckForUpdates() {
        if (UpdateStateStore.shouldAutomaticallyCheckForUpdatesInBackground(context)) {
            checkAttempts = -10; // give more attempts
            this.executor.schedule(this::checkForUpdates, TIME_S_WAIT_BEFORE_CHECK, TimeUnit.SECONDS);
        }
    }

    public void dispose() {
        executor.shutdownNow();
    }

}