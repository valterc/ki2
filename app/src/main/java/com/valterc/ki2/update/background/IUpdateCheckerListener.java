package com.valterc.ki2.update.background;

import com.valterc.ki2.data.update.ReleaseInfo;

public interface IUpdateCheckerListener {

    void onNewUpdateAvailable(ReleaseInfo releaseInfo);

}
