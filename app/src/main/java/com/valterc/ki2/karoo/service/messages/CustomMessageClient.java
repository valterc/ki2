package com.valterc.ki2.karoo.service.messages;

import android.os.Handler;

import com.valterc.ki2.data.message.AudioAlertEventMessage;
import com.valterc.ki2.data.message.AudioAlertMessage;
import com.valterc.ki2.data.message.EnableAntMessage;
import com.valterc.ki2.data.message.Message;
import com.valterc.ki2.data.message.MessageType;
import com.valterc.ki2.data.message.RideStatusMessage;
import com.valterc.ki2.data.message.ShowOverlayMessage;
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
        customMessageHandlers.put(MessageType.SHOW_OVERLAY, new CustomMessageHandler<>(MessageType.SHOW_OVERLAY, ShowOverlayMessage::parse));
        customMessageHandlers.put(MessageType.AUDIO_ALERT, new CustomMessageHandler<>(MessageType.AUDIO_ALERT, AudioAlertMessage::parse));
        customMessageHandlers.put(MessageType.AUDIO_ALERT_EVENT, new CustomMessageHandler<>(MessageType.AUDIO_ALERT_EVENT, AudioAlertEventMessage::parse));
        customMessageHandlers.put(MessageType.ENABLE_ANT, new CustomMessageHandler<>(MessageType.ENABLE_ANT, EnableAntMessage::parse));

        serviceClient.registerMessageWeakListener(onMessage);
    }

    private void onMessage(Message message) {
        CustomMessageHandler<? extends Message> customMessageHandler = customMessageHandlers.get(message.getMessageType());
        if (customMessageHandler != null) {
            customMessageHandler.handleMessage(message);
        }
    }

    /**
     * Register a weak referenced listener that will receive Ride Status messages.
     *
     * @param consumer Consumer that will receive Ride Status messages. It will be referenced using a weak reference so the owner must keep a strong reference.
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
     * Register a weak referenced listener that will receive Update Available messages.
     *
     * @param consumer Consumer that will receive Update Available messages. It will be referenced using a weak reference so the owner must keep a strong reference.
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

    /**
     * Register a weak referenced listener that will receive Show Overlay messages.
     *
     * @param consumer Consumer that will receive Show Overlay messages. It will be referenced using a weak reference so the owner must keep a strong reference.
     */
    public void registerShowOverlayWeakListener(Consumer<ShowOverlayMessage> consumer) {
        handler.post(() -> {
            CustomMessageHandler<ShowOverlayMessage> customMessageHandler =
                    (CustomMessageHandler<ShowOverlayMessage>) customMessageHandlers.get(MessageType.SHOW_OVERLAY);
            if (customMessageHandler != null) {
                customMessageHandler.addListener(consumer);
            }
        });
    }

    /**
     * Register a weak referenced listener that will receive Audio Alert messages.
     *
     * @param consumer Consumer that will receive Audio Alert messages. It will be referenced using a weak reference so the owner must keep a strong reference.
     */
    public void registerAudioAlertWeakListener(Consumer<AudioAlertMessage> consumer) {
        handler.post(() -> {
            CustomMessageHandler<AudioAlertMessage> customMessageHandler =
                    (CustomMessageHandler<AudioAlertMessage>) customMessageHandlers.get(MessageType.AUDIO_ALERT);
            if (customMessageHandler != null) {
                customMessageHandler.addListener(consumer);
            }
        });
    }

    /**
     * Register a weak referenced listener that will receive Audio Alert Event messages.
     *
     * @param consumer Consumer that will receive Audio Alert Event messages. It will be referenced using a weak reference so the owner must keep a strong reference.
     */
    public void registerAudioAlertEventWeakListener(Consumer<AudioAlertEventMessage> consumer) {
        handler.post(() -> {
            CustomMessageHandler<AudioAlertEventMessage> customMessageHandler =
                    (CustomMessageHandler<AudioAlertEventMessage>) customMessageHandlers.get(MessageType.AUDIO_ALERT_EVENT);
            if (customMessageHandler != null) {
                customMessageHandler.addListener(consumer);
            }
        });
    }

    /**
     * Register a weak referenced listener that will receive Enable Ant messages.
     *
     * @param consumer Consumer that will receive Enable Ant messages. It will be referenced using a weak reference so the owner must keep a strong reference.
     */
    public void registerEnableAntWeakListener(Consumer<EnableAntMessage> consumer) {
        handler.post(() -> {
            CustomMessageHandler<EnableAntMessage> customMessageHandler =
                    (CustomMessageHandler<EnableAntMessage>) customMessageHandlers.get(MessageType.ENABLE_ANT);
            if (customMessageHandler != null) {
                customMessageHandler.addListener(consumer);
            }
        });
    }

}
