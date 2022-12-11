package com.valterc.ki2.data.update;

import android.os.Parcel;
import android.os.Parcelable;

import java.time.Instant;
import java.util.Date;

public class ReleaseInfo implements Parcelable {

    private final String name;
    private final String description;
    private final Instant publishedAt;
    private final String url;
    private final String packageName;
    private final String packageUrl;
    private final long packageSizeBytes;

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
    }

    public ReleaseInfo(String name, String description, Instant publishedAt, String url, String packageName, String packageUrl, long packageSizeBytes) {
        this.name = name;
        this.description = description;
        this.publishedAt = publishedAt;
        this.url = url;
        this.packageName = packageName;
        this.packageUrl = packageUrl;
        this.packageSizeBytes = packageSizeBytes;
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
}
