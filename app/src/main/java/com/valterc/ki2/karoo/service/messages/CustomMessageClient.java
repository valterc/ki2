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

@SuppressWarnings({"unchecked", "FieldCanBeLocal"})
public class CustomMessageClient {

    private final Map<MessageType, CustomMessageHandler<? extends Message>> customMessageHandlers;
    private final Handler handler;

    private final Consumer<Message> onMessage = this::onMessage;

    public CustomMessageClient(ServiceClient serviceClient, Handler handler) {
        this.handler = handler;

        customMessageHandlers = new HashMap<>();
        customMessageHandlers.put(MessageType.RIDE_STATUS, new CustomMessageHandler<>(MessageType.RIDE_STATUS, RideStatusMessage::parse));
        customMessageHandlers.put(MessageType.UPDATE_AVAILABLE, new CustomMessageHandler<>(MessageType.UPDATE_AVAILABLE, UpdateAvailableMessage::parse));

        serviceClient.registerMessageWeakListener(onMessage);
    }

    private void onMessage(Message message) {
        CustomMessageHandler<? extends Message> customMessageHandler = customMessageHandlers.get(message.getMessageType());
        if (customMessageHandler != null) {
            customMessageHandler.handleMessage(message);
        }
    }

    /**
     * Register a weak referenced listener that will receive ride status messages.
     *
     * @param consumer Consumer that will receive ride status messages. It will be referenced using a weak reference so the owner must keep a strong reference.
     */
    public void registerRideStatusWeakListener(Consumer<RideStatusMessage> consumer) {
        handler.post(() -> {
            CustomMessageHandler<RideStatusMessage> customMessageHandler =
                    (CustomMessageHandler<RideStatusMessage>) customMessageHandlers.get(MessageType.RIDE_STATUS);
            if (customMessageHandler != null) {
                customMessageHandler.addListener(consumer);
            }
        });
    }

    /**
     * Register a weak referenced listener that will receive update available messages.
     *
     * @param consumer Consumer that will receive update available messages. It will be referenced using a weak reference so the owner must keep a strong reference.
     */
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
