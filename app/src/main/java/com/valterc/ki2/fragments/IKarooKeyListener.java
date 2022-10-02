package com.valterc.ki2.fragments;

import com.valterc.ki2.karoo.input.KarooKey;

/**
 * Interface for an object (typically a Fragment or View) that can receive Karoo key events.
 */
public interface IKarooKeyListener {

    /**
     * Invoked when a Karoo key is pressed.
     *
     * @param karooKey Karoo key that was pressed.
     * @return True if the event should be consumed, False otherwise.
     */
    boolean onKarooKeyPressed(KarooKey karooKey);

}
