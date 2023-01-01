package com.valterc.ki2.karoo.update;

import android.annotation.SuppressLint;
import android.util.Log;

import com.valterc.ki2.data.message.UpdateAvailableMessage;
import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.karoo.handlers.IRideHandler;

import java.util.function.Consumer;

@SuppressWarnings("FieldCanBeLocal")
@SuppressLint("LogNotTimber")
public class UpdateAvailableHandler implements IRideHandler {

    private final Ki2Context context;

    private final Consumer<UpdateAvailableMessage> onUpdateAvailableMessage = this::onUpdateAvailableMessage;

    public UpdateAvailableHandler(Ki2Context context) {
        this.context = context;
        context.getServiceClient().getCustomMessageClient().registerUpdateAvailableWeakListener(onUpdateAvailableMessage);
    }

    private void onUpdateAvailableMessage(UpdateAvailableMessage updateAvailableMessage) {
        Log.d("KI2", "Update available, showing notification");
        UpdateAvailableNotification.showUpdateAvailableNotification(context.getSdkContext(), updateAvailableMessage.getReleaseInfo());
    }

}
