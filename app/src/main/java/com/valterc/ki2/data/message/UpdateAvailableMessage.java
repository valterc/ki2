package com.valterc.ki2.data.message;

import android.os.Bundle;

import com.valterc.ki2.data.update.ReleaseInfo;

public class UpdateAvailableMessage extends Message {

    private static final String KEY_RELEASE_INFO = "releaseInfo";
    private static final  String KEY = "update";

    public static UpdateAvailableMessage parse(Message message) {
        if (!message.getClassType().equals(UpdateAvailableMessage.class.getName())) {
            return null;
        }

        return new UpdateAvailableMessage(message);
    }

    private final ReleaseInfo releaseInfo;

    private UpdateAvailableMessage(Message message) {
        super(message);

        getBundle().setClassLoader(getClass().getClassLoader());
        releaseInfo = getBundle().getParcelable(KEY_RELEASE_INFO);
    }

    public UpdateAvailableMessage(ReleaseInfo releaseInfo) {
        super(KEY, new Bundle(), MessageType.UPDATE_AVAILABLE, true);

        getBundle().putParcelable(KEY_RELEASE_INFO, releaseInfo);

        this.releaseInfo = releaseInfo;
    }

    public ReleaseInfo getReleaseInfo() {
        return releaseInfo;
    }

}