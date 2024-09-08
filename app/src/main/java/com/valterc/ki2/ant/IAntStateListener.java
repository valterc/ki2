package com.valterc.ki2.ant;

public interface IAntStateListener {

    /**
     * Invoked when ANT radio is disabled system wide.
     */
    void onAntDisabled();

    /**
     * Invoked when the state of ANT service changes.
     * @param serviceReady Indicates if ANT service is ready.
     */
    void onAntServiceStateChange(boolean serviceReady);

}
