package com.valterc.ki2.data.update;

public class DownloadedPackageInfo {

    private final ReleaseInfo releaseInfo;
    private final String packagePath;

    public DownloadedPackageInfo(ReleaseInfo releaseInfo, String packagePath) {
        this.releaseInfo = releaseInfo;
        this.packagePath = packagePath;
    }

    public ReleaseInfo getReleaseInfo() {
        return releaseInfo;
    }

    public String getPackagePath() {
        return packagePath;
    }
}
