package com.valterc.ki2.ant;

public interface IAntStateListener {

    /**
     * Invoked when ANT radio is disabled system wide.
     */
    void onAntDisabled();

    /**
     * Invoked when the state of AntManager changes.
     * @param ready Indicates if ANT is ready.
     */
    void onAntStateChange(boolean ready);

}
