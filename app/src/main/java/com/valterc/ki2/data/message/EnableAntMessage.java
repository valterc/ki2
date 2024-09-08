package com.valterc.ki2.data.message;

import android.os.Bundle;

public class EnableAntMessage extends Message {

    public static final String KEY = "enable-ant";

    public static EnableAntMessage parse(Message message) {
        if (!message.getClassType().equals(EnableAntMessage.class.getName())) {
            return null;
        }

        return new EnableAntMessage(message);
    }


    private EnableAntMessage(Message message) {
        super(message);
    }

    public EnableAntMessage() {
        super(KEY, new Bundle(), MessageType.ENABLE_ANT, true);
    }
}
