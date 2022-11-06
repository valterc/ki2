package com.valterc.ki2.data.message;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MessageManager {

    private final ConcurrentHashMap<String, Message> persistentMessages;

    public MessageManager() {
        this.persistentMessages = new ConcurrentHashMap<>();
    }

    public void messageReceived(Message message) {
        if (message == null || message.isEvent()) {
            return;
        }

        persistentMessages.put(message.getKey(), message);
    }

    public void clearMessage(String key) {
        persistentMessages.remove(key);
    }

    public void clearMessages() {
        persistentMessages.clear();
    }

    public List<Message> getMessages() {
        return new ArrayList<>(persistentMessages.values());
    }
}
