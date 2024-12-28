package com.valterc.ki2.update;

import android.content.Context;
import android.os.Environment;

import androidx.annotation.NonNull;

import com.valterc.ki2.data.update.DownloadedPackageInfo;
import com.valterc.ki2.data.update.ReleaseInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import javax.net.ssl.HttpsURLConnection;

import timber.log.Timber;

public class DownloadPackageTask implements Callable<DownloadedPackageInfo> {

    private final Context context;
    private final ReleaseInfo releaseInfo;
    private final Consumer<Float> progressListener;

    public DownloadPackageTask(Context context, ReleaseInfo releaseInfo, Consumer<Float> progressListener) {
        this.context = context;
        this.releaseInfo = releaseInfo;
        this.progressListener = progressListener;
    }

    @NonNull
    @Override
    public DownloadedPackageInfo call() throws Exception {
        try {
            URL url = new URL(releaseInfo.getPackageUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                throw new IOException("Server returned code: " + connection.getResponseCode() + " for URL: " + url);
            }

            File fileDownloadsDirectory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            if (fileDownloadsDirectory == null){
                throw new Exception("Download directory is not available");
            }

            if (fileDownloadsDirectory.exists() && !fileDownloadsDirectory.isDirectory()) {
                if (!fileDownloadsDirectory.delete()) {
                    throw new Exception("Download directory is a file and cannot be deleted");
                }

                if (!fileDownloadsDirectory.mkdirs()) {
                    throw new Exception("Unable to create Download directory");
                }
            }

            Path pathPackageFile = Paths.get(fileDownloadsDirectory.getPath(), releaseInfo.getPackageName());
            Timber.i("Downloading update package to %s", pathPackageFile);

            if (Files.exists(pathPackageFile)) {
                Files.delete(pathPackageFile);
            }

            InputStream inputStream = connection.getInputStream();
            OutputStream outputStream = new FileOutputStream(pathPackageFile.toString());
            byte[] buffer = new byte[1024];
            long totalDownloaded = 0;

            while (true) {
                int read = inputStream.read(buffer);
                if (read == -1) {
                    break;
                }

                outputStream.write(buffer, 0, read);

                if (progressListener != null) {
                    totalDownloaded += read;
                    progressListener.accept(Math.min((float) totalDownloaded / releaseInfo.getPackageSizeBytes(), 1));
                }
            }

            inputStream.close();
            outputStream.close();
            connection.disconnect();

            return new DownloadedPackageInfo(releaseInfo, pathPackageFile.toString());
        } catch (Exception e) {
            Timber.e(e, "Unable to download release package");
            throw e;
        }
    }

}
