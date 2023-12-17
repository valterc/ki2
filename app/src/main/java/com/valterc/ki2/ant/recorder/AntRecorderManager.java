package com.valterc.ki2.ant.recorder;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.valterc.ki2.R;
import com.valterc.ki2.data.preferences.PreferencesView;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class AntRecorderManager {

    private final Context context;
    private AntDebugRecorder recorder;

    public AntRecorderManager(Context context) {
        this.context = context;
        cleanExistingFiles();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        setupRecording(preferences);

        preferences.registerOnSharedPreferenceChangeListener(this::OnSharedPreferenceChangeListener);
    }

    private void OnSharedPreferenceChangeListener(SharedPreferences sharedPreferences, String key) {
        if (key.equals(context.getString(R.string.preference_ant_recording))) {
            setupRecording(sharedPreferences);
        }
    }

    private void cleanExistingFiles() {
        File directory = getDirectory();

        File[] filesToDelete = directory.listFiles(file -> System.currentTimeMillis() - file.lastModified() > TimeUnit.DAYS.toMillis(30));
        if (filesToDelete != null) {
            Timber.i("Cleaning up %s old ANT recording files", filesToDelete.length);
            for (File fileToDelete : filesToDelete) {
                Timber.i("Deleting file: %s", fileToDelete);
                boolean deleted = fileToDelete.delete();
                if (!deleted) {
                    Timber.i("Unable to delete file: %s", fileToDelete);
                }
            }
        }

        filesToDelete = directory.listFiles();
        if (filesToDelete != null) {
            filesToDelete = Arrays.stream(filesToDelete).sorted((a, b) -> Long.compare(b.lastModified(), a.lastModified())).skip(5).toArray(File[]::new);
            Timber.i("Cleaning up %s ANT recording files", filesToDelete.length);
            for (File fileToDelete : filesToDelete) {
                Timber.i("Deleting file: %s", fileToDelete);
                boolean deleted = fileToDelete.delete();
                if (!deleted) {
                    Timber.i("Unable to delete file: %s", fileToDelete);
                }
            }
        }
    }

    private File getDirectory() {
        return new File(context.getDataDir(), "ant-recording");
    }

    private File getReadyDirectory() throws Exception {
        File directory = getDirectory();

        boolean directoryReady = directory.isDirectory() || directory.mkdirs();
        if (!directoryReady) {
            throw new Exception("Directory not ready: " + directory);
        }

        return directory;
    }

    private void setupRecording(SharedPreferences preferences) {
        PreferencesView preferencesView = new PreferencesView(preferences);

        if (preferencesView.isANTRecordingEnabled(context)) {
            if (recorder == null) {
                try {
                    recorder = new AntDebugRecorder(getReadyDirectory());
                    Timber.plant(recorder);
                    Timber.i("Started ANT Recording");
                } catch (Exception e) {
                    Timber.w(e, "Unable to setup ANT recorder manager");
                }
            }
        } else {
            if (recorder != null) {
                try {
                    Timber.uproot(recorder);
                    recorder.close();
                } catch (Exception e) {
                    // ignore
                }

                Timber.i("Stopped ANT Recording");
                recorder = null;
            }
        }
    }

}
