package com.valterc.ki2.data.switches;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class SwitchEvent implements Parcelable {

    private final SwitchChannel channel;
    private final SwitchCommand command;
    private final int repeat;

    public static final Parcelable.Creator<SwitchEvent> CREATOR = new Parcelable.Creator<SwitchEvent>() {
        public SwitchEvent createFromParcel(Parcel in) {
            return new SwitchEvent(in);
        }

        public SwitchEvent[] newArray(int size) {
            return new SwitchEvent[size];
        }
    };

    private SwitchEvent(Parcel in) {
        channel = SwitchChannel.fromValue(in.readInt());
        command = SwitchCommand.fromCommandNumber(in.readInt());
        repeat = in.readInt();
    }

    public SwitchEvent(SwitchChannel channel, SwitchCommand command, int repeat) {
        this.channel = channel;
        this.command = command;
        this.repeat = repeat;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(channel.getValue());
        out.writeInt(command.getCommandNumber());
        out.writeInt(repeat);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public SwitchChannel getChannel() {
        return channel;
    }

    public SwitchCommand getCommand() {
        return command;
    }

    public int getRepeat() {
        return repeat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SwitchEvent that = (SwitchEvent) o;

        if (repeat != that.repeat) return false;
        if (channel != that.channel) return false;
        return command == that.command;
    }

    @Override
    public int hashCode() {
        int result = channel != null ? channel.hashCode() : 0;
        result = 31 * result + (command != null ? command.hashCode() : 0);
        result = 31 * result + repeat;
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "SwitchEvent{" +
                "channel=" + channel +
                ", command=" + command +
                ", repeat=" + repeat +
                '}';
    }
}
