package com.valterc.ki2.update;

import androidx.annotation.NonNull;

import com.valterc.ki2.data.update.ReleaseInfo;

import java.util.concurrent.Callable;

public class GetLatestReleaseInfoTask implements Callable<ReleaseInfo> {

    @NonNull
    @Override
    public ReleaseInfo call() throws Exception {
        throw new Exception("This version of Ki2 using the Karoo SDK is no longer supported. Please find a new version at \n\nhttps://github.com/valterc/ki2");
    }

}