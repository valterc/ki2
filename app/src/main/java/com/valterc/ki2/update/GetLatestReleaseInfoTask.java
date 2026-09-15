package com.valterc.ki2.update;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.valterc.ki2.data.update.ReleaseInfo;
import com.valterc.ki2.data.update.Version;

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
    public static final String URL_RELEASES = "https://api.github.com/repos/valterc/ki2/releases?per_page=30";
    public static final String KEY_ASSETS = "assets";
    public static final String KEY_DESCRIPTION = "body";
    public static final String KEY_DRAFT = "draft";
    public static final String KEY_NAME = "name";
    public static final String KEY_PACKAGE_SIZE = "size";
    public static final String KEY_PACKAGE_URL = "browser_download_url";
    public static final String KEY_PRERELEASE = "prerelease";
    public static final String KEY_PUBLISHED_DATE = "published_at";
    public static final String KEY_TAG_NAME = "tag_name";
    public static final String KEY_URL = "html_url";
    public static final String PATTERN_PACKAGE_NAME_END = "release.apk";

    private final boolean includePreviewReleases;

    public GetLatestReleaseInfoTask() {
        this(false);
    }

    /**
     * Create a task that gets the latest release information.
     * <p>
     * The latest release is always the most recent release published in GitHub, it is not
     * necessarily a release with a higher version than the current application version. This
     * allows a release to be removed or changed in GitHub in order to downgrade the application.
     *
     * @param includePreviewReleases Indicates if preview releases should be considered. When true,
     *                               the latest release is returned regardless of it being a preview
     *                               release or a stable release.
     */
    public GetLatestReleaseInfoTask(boolean includePreviewReleases) {
        this.includePreviewReleases = includePreviewReleases;
    }

    @NonNull
    @Override
    public ReleaseInfo call() throws Exception {
        try {
            if (includePreviewReleases) {
                return getLatestReleaseIncludingPreviews();
            }

            return getLatestStableRelease();
        } catch (Exception e) {
            Timber.e(e, "Unable to check for updates");
            throw e;
        }
    }

    @NonNull
    private ReleaseInfo getLatestStableRelease() throws IOException {
        JsonObject jsonObject = getJson(URL_LATEST_RELEASE).getAsJsonObject();
        ReleaseInfo releaseInfo = parseRelease(jsonObject);

        if (releaseInfo == null) {
            throw new IOException("No valid asset in latest release");
        }

        return releaseInfo;
    }

    @NonNull
    private ReleaseInfo getLatestReleaseIncludingPreviews() throws IOException {
        JsonArray jsonArrayReleases = getJson(URL_RELEASES).getAsJsonArray();

        for (int i = 0; i < jsonArrayReleases.size(); i++) {
            JsonObject jsonRelease = jsonArrayReleases.get(i).getAsJsonObject();

            if (getBoolean(jsonRelease, KEY_DRAFT)) {
                continue;
            }

            ReleaseInfo releaseInfo = parseRelease(jsonRelease);
            if (releaseInfo != null) {
                return releaseInfo;
            }
        }

        throw new IOException("No valid release available");
    }

    @NonNull
    private static JsonElement getJson(String urlValue) throws IOException {
        URL url = new URL(urlValue);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                throw new IOException("Server returned code: " + connection.getResponseCode() + " for URL: " + url);
            }

            try (InputStreamReader inputStream = new InputStreamReader(connection.getInputStream());
                 BufferedReader bufferedReader = new BufferedReader(inputStream)) {
                return JsonParser.parseReader(bufferedReader);
            }
        } finally {
            connection.disconnect();
        }
    }

    @Nullable
    private static ReleaseInfo parseRelease(JsonObject jsonObject) {
        JsonElement jsonElementAssets = jsonObject.get(KEY_ASSETS);
        if (jsonElementAssets == null || !jsonElementAssets.isJsonArray()) {
            return null;
        }

        JsonArray jsonArrayAssets = jsonElementAssets.getAsJsonArray();

        String packageName = null;
        String packageUrl = null;
        long packageSizeBytes = -1;

        for (int i = 0; i < jsonArrayAssets.size(); i++) {
            JsonObject jsonAssetObject = jsonArrayAssets.get(i).getAsJsonObject();

            if (jsonAssetObject.get(KEY_NAME).getAsString().endsWith(PATTERN_PACKAGE_NAME_END)) {
                packageName = jsonAssetObject.get(KEY_NAME).getAsString();
                packageUrl = jsonAssetObject.get(KEY_PACKAGE_URL).getAsString();
                packageSizeBytes = jsonAssetObject.get(KEY_PACKAGE_SIZE).getAsLong();
            }
        }

        if (packageName == null) {
            return null;
        }

        String name = getString(jsonObject, KEY_TAG_NAME);
        if (name == null) {
            name = getString(jsonObject, KEY_NAME);
        }

        if (name == null) {
            return null;
        }

        String description = getString(jsonObject, KEY_DESCRIPTION);
        String publishedDate = getString(jsonObject, KEY_PUBLISHED_DATE);

        return new ReleaseInfo(
                name,
                description == null ? "" : description,
                publishedDate == null ? Instant.EPOCH : Instant.parse(publishedDate),
                getString(jsonObject, KEY_URL),
                packageName,
                packageUrl,
                packageSizeBytes,
                getBoolean(jsonObject, KEY_PRERELEASE) || Version.isPreview(name));
    }

    @Nullable
    private static String getString(JsonObject jsonObject, String key) {
        JsonElement jsonElement = jsonObject.get(key);
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return null;
        }

        return jsonElement.getAsString();
    }

    private static boolean getBoolean(JsonObject jsonObject, String key) {
        JsonElement jsonElement = jsonObject.get(key);
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return false;
        }

        return jsonElement.getAsBoolean();
    }
}
