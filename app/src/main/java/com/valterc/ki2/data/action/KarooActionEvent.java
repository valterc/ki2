package com.valterc.ki2.data.action;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class KarooActionEvent implements Parcelable {

    private final KarooAction action;
    private final int repeat;
    private final int replicate;

    public static final Parcelable.Creator<KarooActionEvent> CREATOR = new Parcelable.Creator<>() {
        public KarooActionEvent createFromParcel(Parcel in) {
            return new KarooActionEvent(in);
        }

        public KarooActionEvent[] newArray(int size) {
            return new KarooActionEvent[size];
        }
    };

    private KarooActionEvent(Parcel in) {
        action = KarooAction.fromOrdinal(in.readInt());
        repeat = in.readInt();
        replicate = in.readInt();
    }

    public KarooActionEvent(KarooActionEvent actionEvent, int replicate)
    {
        this(actionEvent.action, actionEvent.repeat, replicate);
    }

    public KarooActionEvent(KarooAction action, int repeat)
    {
        this(action, repeat, 1);
    }

    public KarooActionEvent(KarooAction action, int repeat, int replicate)
    {
        this.action = action;
        this.repeat = repeat;
        this.replicate = replicate;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(action.ordinal());
        out.writeInt(repeat);
        out.writeInt(replicate);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public KarooAction getAction() {
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

        KarooActionEvent that = (KarooActionEvent) o;

        if (repeat != that.repeat) return false;
        if (replicate != that.replicate) return false;
        return action == that.action;
    }

    @Override
    public int hashCode() {
        int result = action != null ? action.hashCode() : 0;
        result = 31 * result + repeat;
        result = 31 * result + replicate;
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "KarooActionEvent{" +
                "action=" + action +
                ", repeat=" + repeat +
                ", replicate=" + replicate +
                '}';
    }
}
