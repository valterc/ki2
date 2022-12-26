package com.valterc.ki2.karoo.service.messages;

import com.valterc.ki2.data.message.Message;
import com.valterc.ki2.data.message.MessageType;
import com.valterc.ki2.karoo.service.DataStreamWeakListenerList;

import java.util.function.Consumer;
import java.util.function.Function;

public class CustomMessageHandler<TMessage> {

    private final MessageType messageType;
    private final Function<Message, TMessage> messageParser;
    private final DataStreamWeakListenerList<TMessage> listeners;

    public CustomMessageHandler(MessageType messageType, Function<Message, TMessage> messageParser) {
        this.messageType = messageType;
        this.messageParser = messageParser;
        listeners = new DataStreamWeakListenerList<>();
    }

    public void handleMessage(Message message) {
        if (message.getMessageType() != messageType){
            return;
        }

        TMessage parsedMessage = messageParser.apply(message);
        if (parsedMessage != null) {
            listeners.pushData(parsedMessage);
        }
    }

    public void addListener(Consumer<TMessage> messageConsumer) {
        listeners.addListener(messageConsumer);
    }

}
