package com.valterc.ki2.karoo.handlers;

/**
 * Represent a handler that performs actions during a ride.
 */
public interface IRideHandler {

    /**
     * Invoked when a ride starts.
     */
    default void onRideStart() {
    }

    /**
     * Invoked when a ride is paused.
     */
    default void onRidePause() {
    }

    /**
     * Invoked when a ride is resumed.
     */
    default void onRideResume() {
    }

    /**
     * Invoked when a ride ends.
     */
    default void onRideEnd() {
    }

}
