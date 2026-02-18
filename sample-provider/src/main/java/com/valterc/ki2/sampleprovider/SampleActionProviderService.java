package com.valterc.ki2.sampleprovider;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Minimal service used only for Ki2 discovery via queryIntentServices().
 * All action handling is done by {@link SampleActionProviderReceiver}.
 */
public class SampleActionProviderService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
