package com.valterc.ki2.data.input;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class KarooKeyEvent implements Parcelable {

    private final KarooKey key;
    private final KeyAction action;
    private final int repeat;
    private final int replicate;

    public static final Parcelable.Creator<KarooKeyEvent> CREATOR = new Parcelable.Creator<KarooKeyEvent>() {
        public KarooKeyEvent createFromParcel(Parcel in) {
            return new KarooKeyEvent(in);
        }

        public KarooKeyEvent[] newArray(int size) {
            return new KarooKeyEvent[size];
        }
    };

    private KarooKeyEvent(Parcel in) {
        key = KarooKey.fromKeyCode(in.readInt());
        action = KeyAction.fromActionNumber(in.readInt());
        repeat = in.readInt();
        replicate = in.readInt();
    }

    public KarooKeyEvent(KarooKeyEvent keyEvent, int replicate)
    {
        this(keyEvent.key, keyEvent.action, keyEvent.repeat, replicate);
    }

    public KarooKeyEvent(KarooKey key, KeyAction action, int repeat)
    {
        this(key, action, repeat, 1);
    }

    public KarooKeyEvent(KarooKey key, KeyAction action, int repeat, int replicate)
    {
        this.key = key;
        this.action = action;
        this.repeat = repeat;
        this.replicate = replicate;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(key.getKeyCode());
        out.writeInt(action.getActionNumber());
        out.writeInt(repeat);
        out.writeInt(replicate);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public KarooKey getKey() {
        return key;
    }

    public KeyAction getAction() {
        return action;
    }

    public int getRepeat() {
        return repeat;
    }

    public int getReplicate() {
        return replicate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KarooKeyEvent that = (KarooKeyEvent) o;

        if (repeat != that.repeat) return false;
        if (replicate != that.replicate) return false;
        if (key != that.key) return false;
        return action == that.action;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (action != null ? action.hashCode() : 0);
        result = 31 * result + repeat;
        result = 31 * result + replicate;
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "KarooKeyEvent{" +
                "key=" + key +
                ", action=" + action +
                ", repeat=" + repeat +
                ", replicate=" + replicate +
                '}';
    }
}
