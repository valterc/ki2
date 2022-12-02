package com.valterc.ki2.data.update;

import java.util.Date;

public class ReleaseInfo {

    private final String name;
    private final String description;
    private final Date publishedAt;
    private final String url;
    private final String packageName;
    private final String packageUrl;
    private final long packageSizeBytes;

    public ReleaseInfo(String name, String description, Date publishedAt, String url, String packageName, String packageUrl, long packageSizeBytes) {
        this.name = name;
        this.description = description;
        this.publishedAt = publishedAt;
        this.url = url;
        this.packageName = packageName;
        this.packageUrl = packageUrl;
        this.packageSizeBytes = packageSizeBytes;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Date getPublishedAt() {
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
