// IPreferencesCallback.aidl
package com.valterc.ki2.services.callbacks;

import com.valterc.ki2.data.preferences.PreferencesView;

interface IPreferencesCallback {

    void onPreferences(in PreferencesView preferenceView);

}