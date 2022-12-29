package com.valterc.ki2.karoo.service.messages;

import android.os.Handler;

import com.valterc.ki2.data.message.Message;
import com.valterc.ki2.data.message.MessageType;
import com.valterc.ki2.data.message.RideStatusMessage;
import com.valterc.ki2.data.message.UpdateAvailableMessage;
import com.valterc.ki2.karoo.service.ServiceClient;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public class CustomMessageClient {

    private final Map<MessageType, CustomMessageHandler<? extends Message>> customMessageHandlers;
    private final Handler handler;

    public CustomMessageClient(ServiceClient serviceClient, Handler handler) {
        this.handler = handler;

        customMessageHandlers = new HashMap<>();
        customMessageHandlers.put(MessageType.RIDE_STATUS, new CustomMessageHandler<>(MessageType.RIDE_STATUS, RideStatusMessage::parse));
        customMessageHandlers.put(MessageType.UPDATE_AVAILABLE, new CustomMessageHandler<>(MessageType.UPDATE_AVAILABLE, UpdateAvailableMessage::parse));

        serviceClient.registerMessageWeakListener(this::onMessage);
    }

    private void onMessage(Message message) {
        CustomMessageHandler<? extends Message> customMessageHandler = customMessageHandlers.get(message.getMessageType());
        if (customMessageHandler != null) {
            customMessageHandler.handleMessage(message);
        }
    }

    public void registerRideStatusWeakListener(Consumer<RideStatusMessage> consumer) {
        handler.post(() -> {
            CustomMessageHandler<RideStatusMessage> customMessageHandler =
                    (CustomMessageHandler<RideStatusMessage>) customMessageHandlers.get(MessageType.RIDE_STATUS);
            if (customMessageHandler != null) {
                customMessageHandler.addListener(consumer);
            }
        });
    }

    public void registerUpdateAvailableWeakListener(Consumer<UpdateAvailableMessage> consumer) {
        handler.post(() -> {
            CustomMessageHandler<UpdateAvailableMessage> customMessageHandler =
                    (CustomMessageHandler<UpdateAvailableMessage>) customMessageHandlers.get(MessageType.UPDATE_AVAILABLE);
            if (customMessageHandler != null) {
                customMessageHandler.addListener(consumer);
            }
        });
    }

}
