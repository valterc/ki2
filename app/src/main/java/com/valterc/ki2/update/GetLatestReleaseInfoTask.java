package com.valterc.ki2.update;

import androidx.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.valterc.ki2.data.update.ReleaseInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.concurrent.Callable;

import javax.net.ssl.HttpsURLConnection;

import timber.log.Timber;

public class GetLatestReleaseInfoTask implements Callable<ReleaseInfo> {

    public static final String URL_LATEST_RELEASE = "https://api.github.com/repos/valterc/ki2/releases/latest";
    public static final String KEY_ASSETS = "assets";
    public static final String KEY_DESCRIPTION = "body";
    public static final String KEY_NAME = "name";
    public static final String KEY_PACKAGE_SIZE = "size";
    public static final String KEY_PACKAGE_URL = "browser_download_url";
    public static final String KEY_PUBLISHED_DATE = "published_at";
    public static final String KEY_URL = "html_url";
    public static final String PATTERN_PACKAGE_NAME_END = "release.apk";

    @NonNull
    @Override
    public ReleaseInfo call() throws Exception {
        try {
            URL url = new URL(URL_LATEST_RELEASE);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                throw new IOException("Server returned code: " + connection.getResponseCode() + " for URL: " + url);
            }

            InputStreamReader inputStream = new InputStreamReader(connection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStream);
            JsonElement jsonElement = JsonParser.parseReader(bufferedReader);

            inputStream.close();
            connection.disconnect();

            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonArray jsonArrayAssets = jsonObject.get(KEY_ASSETS).getAsJsonArray();

            if (jsonArrayAssets.isEmpty()) {
                throw new Exception("No asset available in JSON response");
            }

            String packageName = null;
            String packageUrl = null;
            long packageSizeBytes = -1;

            for (int i = 0; i < jsonArrayAssets.size(); i++) {
                JsonElement jsonAsset = jsonArrayAssets.get(i);
                JsonObject jsonAssetObject = jsonAsset.getAsJsonObject();

                if (jsonAssetObject.get(KEY_NAME).getAsString().endsWith(PATTERN_PACKAGE_NAME_END)) {
                    packageName = jsonAssetObject.get(KEY_NAME).getAsString();
                    packageUrl = jsonAssetObject.get(KEY_PACKAGE_URL).getAsString();
                    packageSizeBytes = jsonAssetObject.get(KEY_PACKAGE_SIZE).getAsLong();
                }
            }

            if (packageName == null) {
                throw new Exception("No valid asset in latest release");
            }

            return new ReleaseInfo(
                    jsonObject.get(KEY_NAME).getAsString(),
                    jsonObject.get(KEY_DESCRIPTION).getAsString(),
                    Instant.parse(jsonObject.get(KEY_PUBLISHED_DATE).getAsString()),
                    jsonObject.get(KEY_URL).getAsString(),
                    packageName,
                    packageUrl,
                    packageSizeBytes);

        } catch (Exception e) {
            Timber.e(e, "Unable to check for updates");
            throw e;
        }
    }

}