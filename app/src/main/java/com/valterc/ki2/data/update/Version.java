package com.valterc.ki2.data.update;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Application version, as defined by the git tag used to create a GitHub release.
 * <p>
 * Stable versions use a numeric only format, for example: {@code 15.0}.
 * Preview versions append a preview suffix to the base version, for example: {@code 16.0-preview.1}.
 * <p>
 * Versions are not ordered. The version that the application updates to is always the latest
 * release published in GitHub, which allows a release to be removed or changed in GitHub in order
 * to downgrade the application.
 */
public final class Version {

    public static final String PREVIEW_IDENTIFIER = "preview";

    private static final Pattern PATTERN_VERSION = Pattern.compile(
            "^v?\\d+(?:\\.\\d+)*(-" + PREVIEW_IDENTIFIER + "(?:[.\\-]?\\d+)?)?$",
            Pattern.CASE_INSENSITIVE);

    /**
     * Parse a version.
     *
     * @param value Version value, example: {@code 16.0-preview.1}.
     * @return Parsed version or null if the given value is not a valid version.
     */
    @Nullable
    public static Version parse(@Nullable String value) {
        if (value == null) {
            return null;
        }

        Matcher matcher = PATTERN_VERSION.matcher(value.trim());
        if (!matcher.matches()) {
            return null;
        }

        return new Version(value.trim(), matcher.group(1) != null);
    }

    /**
     * Indicates if the given version value represents a preview version.
     *
     * @param value Version value.
     * @return True if the given value is a valid preview version, false otherwise.
     */
    public static boolean isPreview(@Nullable String value) {
        Version version = parse(value);
        return version != null && version.isPreview();
    }

    private final String value;
    private final boolean preview;

    private Version(String value, boolean preview) {
        this.value = value;
        this.preview = preview;
    }

    /**
     * Indicates if this version is a preview version.
     *
     * @return True if this is a preview version, false otherwise.
     */
    public boolean isPreview() {
        return preview;
    }

    @NonNull
    @Override
    public String toString() {
        return value;
    }

}
