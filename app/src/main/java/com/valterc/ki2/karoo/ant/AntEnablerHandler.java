package com.valterc.ki2.karoo.ant;

import android.annotation.SuppressLint;

import com.valterc.ki2.data.message.EnableAntMessage;
import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.karoo.handlers.IRideHandler;
import com.valterc.ki2.karoo.hooks.ActivityServiceAntHook;

import java.util.function.Consumer;

@SuppressWarnings("FieldCanBeLocal")
@SuppressLint("LogNotTimber")
public class AntEnablerHandler implements IRideHandler {

    private final Ki2Context context;

    private final Consumer<EnableAntMessage> onEnableAntMessage = this::onEnableAntMessage;

    public AntEnablerHandler(Ki2Context context) {
        this.context = context;
        context.getServiceClient().getCustomMessageClient().registerEnableAntWeakListener(onEnableAntMessage);

        ActivityServiceAntHook.ensureAntEnabled(context.getSdkContext().getBaseContext());
    }

    private void onEnableAntMessage(EnableAntMessage updateAvailableMessage) {
        ActivityServiceAntHook.ensureAntEnabled(context.getSdkContext().getBaseContext());
    }

}
