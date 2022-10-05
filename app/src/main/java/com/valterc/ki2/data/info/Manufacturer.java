package com.valterc.ki2.data.info;

import java.util.Arrays;
import java.util.Optional;

public enum Manufacturer {

    GARMIN(1, "Garmin"),
    SHIMANO(41, "Shimano"),
    HAMMERHEAD(289, "Hammerhead"),
    UNKNOWN(-1, "Unknown");

    public static Manufacturer fromId(int id) {
        Optional<Manufacturer> element =
                Arrays.stream(Manufacturer.values()).filter(s -> s.id == id)
                        .findFirst();

        return element.orElse(UNKNOWN);
    }

    private final int id;
    private final String name;

    Manufacturer(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
