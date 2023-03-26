package com.valterc.ki2.data.shifting;

/**
 * Shifting limit type.
 */
public enum ShiftingLimitType {

    /**
     * Not on shifting limit.
     */
    NONE,

    /**
     * Lower shifting limit, both front and rear gears at min.
     */
    LOWER_LIMIT,

    /**
     * Higher shifting limit, both front and rear gears at max.
     */
    HIGHER_LIMIT

}
