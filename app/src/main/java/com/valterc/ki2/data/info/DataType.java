package com.valterc.ki2.data.info;

public enum DataType {

    UNKNOWN(0, false),
    BATTERY(1, false),
    SHIFTING(2, false),
    SWITCH(3, true),
    KEY(4, true),
    MANUFACTURER_INFO(5, false),
    SIGNAL(6, true),
    OTHER(255, false);

    public static DataType fromFlag(int flag) {
        for (DataType s : DataType.values()) {
            if (s.flag == flag) {
                return s;
            }
        }

        return UNKNOWN;
    }

    private final int flag;
    private final boolean event;

    DataType(int flag, boolean event) {
        this.flag = flag;
        this.event = event;
    }

    public int getFlag() {
        return flag;
    }

    public boolean isEvent() {
        return event;
    }
}
