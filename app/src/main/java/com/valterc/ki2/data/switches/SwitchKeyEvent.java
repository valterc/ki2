package com.valterc.ki2.data.switches;

import android.os.Parcel;
import android.os.Parcelable;

import com.valterc.ki2.karoo.input.KarooKey;

public class SwitchKeyEvent implements Parcelable {

    private KarooKey key;
    private SwitchCommand command;
    private int repeat;

    public static final Parcelable.Creator<SwitchKeyEvent> CREATOR = new Parcelable.Creator<SwitchKeyEvent>() {
        public SwitchKeyEvent createFromParcel(Parcel in) {
            return new SwitchKeyEvent(in);
        }

        public SwitchKeyEvent[] newArray(int size) {
            return new SwitchKeyEvent[size];
        }
    };

    private SwitchKeyEvent(Parcel in) {
        readFromParcel(in);
    }

    public SwitchKeyEvent(KarooKey key, SwitchCommand command)
    {
        this(key, command, 0);
    }

    public SwitchKeyEvent(KarooKey key, SwitchCommand command, int repeat)
    {
        this.key = key;
        this.command = command;
        this.repeat = repeat;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(key.getKeyCode());
        out.writeInt(command.getCommandNumber());
        out.writeInt(repeat);
    }

    public void readFromParcel(Parcel in) {
        key = KarooKey.fromKeyCode(in.readInt());
        command = SwitchCommand.fromCommandNumber(in.readInt());
        repeat = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public KarooKey getKey() {
        return key;
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

        SwitchKeyEvent that = (SwitchKeyEvent) o;

        if (repeat != that.repeat) return false;
        if (key != that.key) return false;
        return command == that.command;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (command != null ? command.hashCode() : 0);
        result = 31 * result + repeat;
        return result;
    }
}
