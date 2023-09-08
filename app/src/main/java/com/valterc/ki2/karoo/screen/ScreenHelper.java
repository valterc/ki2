package com.valterc.ki2.karoo.screen;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.utils.ActivityUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

@SuppressLint("LogNotTimber")
@SuppressWarnings("FieldCanBeLocal")
public class ScreenHelper {

    @SuppressLint("SimpleDateFormat")
    private final static DateFormat DATE_FORMAT_FILE_NAME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private final static int TIME_MS_ACQUIRE_LOCK_TIMEOUT = 1000;

    private final Ki2Context ki2Context;

    public ScreenHelper(Ki2Context ki2Context) {
        this.ki2Context = ki2Context;
    }

    @SuppressWarnings("deprecation")
    public void turnScreenOn() {
        PowerManager powerManager = (PowerManager) ki2Context.getSdkContext().getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE,
                "Ki2::WakeLock");

        wakeLock.acquire(TIME_MS_ACQUIRE_LOCK_TIMEOUT);
        wakeLock.release();
    }

    public boolean takeScreenshot() {
        Activity activity = ActivityUtils.getRunningActivity();
        if (activity == null) {
            return false;
        }

        try {
            File directoryPictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            if (directoryPictures == null) {
                return false;
            }

            boolean directoryReady = directoryPictures.isDirectory() || directoryPictures.mkdirs();
            if (!directoryReady) {
                return false;
            }

            Window window = activity.getWindow();
            if (window == null) {
                return false;
            }

            View view = window.getDecorView();

            Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
            try {
                Canvas canvas = new Canvas(bitmap);
                view.draw(canvas);

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

                File fileScreenshot = new File(directoryPictures, DATE_FORMAT_FILE_NAME.format(Date.from(Instant.now())) + ".png");
                boolean fileReady = fileScreenshot.createNewFile();
                if (!fileReady) {
                    return false;
                }

                FileOutputStream fileOutputStream = new FileOutputStream(fileScreenshot);
                fileOutputStream.write(outputStream.toByteArray());
                fileOutputStream.close();

                Log.i("KI2", "Saved screenshot in: " + fileScreenshot.getPath());
            } finally {
                bitmap.recycle();
            }

            return true;
        } catch (Exception exception) {
            Log.w("KI2", "Unable to take screenshot", exception);
            return false;
        }
    }

}
