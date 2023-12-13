package com.valterc.ki2.ant.recorder;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.valterc.ki2.ant.AntManager;
import com.valterc.ki2.ant.connection.AntConnectionManager;
import com.valterc.ki2.ant.connection.AntDeviceConnection;
import com.valterc.ki2.ant.connection.handler.profile.ShimanoEBikeProfileHandler;
import com.valterc.ki2.ant.connection.handler.profile.ShimanoShiftingProfileHandler;
import com.valterc.ki2.ant.connection.handler.transport.TransportHandler;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

@SuppressLint("SimpleDateFormat")
public class AntDebugRecorder extends Timber.DebugTree implements Closeable {


    private final static DateFormat DATE_FORMAT_FILE_NAME = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.SSS");
    private final static DateFormat DATE_FORMAT_LOG_TIME = new SimpleDateFormat("HH:mm:ss.SSS");

    private final static Set<String> TAGS = new HashSet<>(Arrays.asList(
            ShimanoShiftingProfileHandler.class.getSimpleName(),
            ShimanoEBikeProfileHandler.class.getSimpleName(),
            TransportHandler.class.getSimpleName(),
            AntManager.class.getSimpleName(),
            AntConnectionManager.class.getSimpleName(),
            AntDeviceConnection.class.getSimpleName()
    ));

    private final Queue<String> queueLogs;
    private final ScheduledExecutorService executorService;
    private final FileWriter fileWriter;

    public AntDebugRecorder(File directory) throws Exception {
        queueLogs = new ConcurrentLinkedQueue<>();
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(this::persistLogs, 5, 5, TimeUnit.SECONDS);

        File fileRecording = new File(directory, "ki2-ant_recording-" + DATE_FORMAT_FILE_NAME.format(Date.from(Instant.now())) + ".txt");
        boolean fileReady = fileRecording.createNewFile();
        if (!fileReady) {
            throw new Exception("File not ready");
        }

        fileWriter = new FileWriter(fileRecording);
    }

    private void persistLogs() {
        String logMessage;
        while ((logMessage = queueLogs.poll()) != null) {
            try {
                fileWriter.write(logMessage);
            } catch (Exception e) {
                // ignore
            }
        }

        try {
            fileWriter.flush();
        } catch (IOException e) {
            // ignore
        }
    }

    @Override
    protected boolean isLoggable(@Nullable String tag, int priority) {
        return tag != null && TAGS.contains(tag);
    }

    @Override
    protected void log(int priority, @Nullable String tag, @NonNull String message, @Nullable Throwable t) {
        if (executorService.isShutdown()) {
            return;
        }

        queueLogs.add(DATE_FORMAT_LOG_TIME.format(new Date()) + " - " + getPriorityString(priority) + " [" + tag + "] " + message + (t == null ? "" : "\n" + t.getMessage() + "\n" + Log.getStackTraceString(t)) + "\n");
    }

    private String getPriorityString(int priority) {
        switch (priority) {
            case Log.VERBOSE:
                return "V";
            case Log.DEBUG:
                return "D";
            case Log.INFO:
                return "I";
            case Log.WARN:
                return "W";
            case Log.ERROR:
                return "E";
            case Log.ASSERT:
                return "A";
        }

        return "_";
    }

    @Override
    public void close() throws IOException {
        executorService.shutdown();
        queueLogs.clear();
        fileWriter.close();
    }
}
