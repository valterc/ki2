package com.valterc.ki2.data.message;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class Message implements Parcelable {

    private final String key;
    private final Bundle bundle;
    private final MessageType messageType;
    private final boolean persistent;
    private final String classType;

    public static Message createEvent(String key, MessageType messageType) {
        return new Message(key, null, messageType, false);
    }

    public static Message createEvent(String key, Bundle bundle, MessageType messageType) {
        return new Message(key, bundle, messageType, false);
    }

    public static Message createPersistent(String key, MessageType messageType) {
        return new Message(key, null, messageType, true);
    }

    public static Message createPersistent(String key, Bundle bundle, MessageType messageType) {
        return new Message(key, bundle, messageType, true);
    }

    public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    protected Message(Message message) {
        this.key = message.key;
        this.bundle = message.bundle;
        this.messageType = message.messageType;
        this.persistent = message.persistent;
        this.classType = getClass().getName();
    }

    protected Message(String key, Bundle bundle, MessageType messageType, boolean persistent) {
        this.key = key;
        this.bundle = bundle;
        this.messageType = messageType;
        this.persistent = persistent;
        this.classType = getClass().getName();
    }

    private Message(Parcel in) {
        key = in.readString();
        bundle = in.readBundle(getClass().getClassLoader());
        messageType = MessageType.fromValue(in.readInt());
        persistent = in.readByte() == 1;
        classType = in.readString();
    }

    public String getKey() {
        return key;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public boolean isEvent() {
        return !persistent;
    }

    public boolean isPersistent() {
        return persistent;
    }

    protected String getClassType() {
        return classType;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(key);
        out.writeBundle(bundle);
        out.writeInt(messageType.getValue());
        out.writeByte((byte) (persistent ? 1 : 0));
        out.writeString(classType);
    }

    @Override
    public int describeContents() {
        return 0;
    }

}
