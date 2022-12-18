package com.valterc.ki2.data.switches;

import android.os.Parcel;
import android.os.Parcelable;

public class SwitchEvent implements Parcelable {

    private final SwitchType type;
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
        type = SwitchType.fromValue(in.readInt());
        command = SwitchCommand.fromCommandNumber(in.readInt());
        repeat = in.readInt();
    }

    public SwitchEvent(SwitchType type, SwitchCommand command, int repeat) {
        this.type = type;
        this.command = command;
        this.repeat = repeat;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(type.getValue());
        out.writeInt(command.getCommandNumber());
        out.writeInt(repeat);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public SwitchType getType() {
        return type;
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
        if (type != that.type) return false;
        return command == that.command;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (command != null ? command.hashCode() : 0);
        result = 31 * result + repeat;
        return result;
    }
}
