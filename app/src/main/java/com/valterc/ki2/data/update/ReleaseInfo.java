package com.valterc.ki2.data.update;

import android.os.Parcel;
import android.os.Parcelable;

import java.time.Instant;
import java.util.Objects;

public class ReleaseInfo implements Parcelable {

    private final String name;
    private final String description;
    private final Instant publishedAt;
    private final String url;
    private final String packageName;
    private final String packageUrl;
    private final long packageSizeBytes;
    private final boolean preview;

    public static final Parcelable.Creator<ReleaseInfo> CREATOR = new Parcelable.Creator<ReleaseInfo>() {
        public ReleaseInfo createFromParcel(Parcel in) {
            return new ReleaseInfo(in);
        }

        public ReleaseInfo[] newArray(int size) {
            return new ReleaseInfo[size];
        }
    };

    private ReleaseInfo(Parcel in) {
        this.name = in.readString();
        this.description = in.readString();
        this.publishedAt = Instant.ofEpochMilli(in.readLong());
        this.url = in.readString();
        this.packageName = in.readString();
        this.packageUrl = in.readString();
        this.packageSizeBytes = in.readLong();
        this.preview = in.readByte() == 1;
    }

    public ReleaseInfo(String name, String description, Instant publishedAt, String url, String packageName, String packageUrl, long packageSizeBytes, boolean preview) {
        this.name = name;
        this.description = description;
        this.publishedAt = publishedAt;
        this.url = url;
        this.packageName = packageName;
        this.packageUrl = packageUrl;
        this.packageSizeBytes = packageSizeBytes;
        this.preview = preview;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(name);
        out.writeString(description);
        out.writeLong(publishedAt.toEpochMilli());
        out.writeString(url);
        out.writeString(packageName);
        out.writeString(packageUrl);
        out.writeLong(packageSizeBytes);
        out.writeByte(preview ? (byte) 1 : 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public String getUrl() {
        return url;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getPackageUrl() {
        return packageUrl;
    }

    public long getPackageSizeBytes() {
        return packageSizeBytes;
    }

    /**
     * Indicates if this release is a preview release.
     *
     * @return True if this is a preview release, false otherwise.
     */
    public boolean isPreview() {
        return preview;
    }

    /**
     * Indicates if this release should be applied as an update over the given version.
     * <p>
     * Any release that is different from the given version is considered an update, including
     * older releases. This allows a release to be removed or changed in GitHub in order to
     * downgrade the application.
     *
     * @param currentVersion Version currently installed.
     * @return True if this release is an update over the given version, false otherwise.
     */
    public boolean isUpdateFrom(String currentVersion) {
        return !Objects.equals(name, currentVersion);
    }
}
