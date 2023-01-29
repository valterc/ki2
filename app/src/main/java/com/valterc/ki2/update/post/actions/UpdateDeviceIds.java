package com.valterc.ki2.update.post.actions;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.device.DeviceStore;
import com.valterc.ki2.update.PostUpdateContext;

import java.util.HashSet;
import java.util.Set;

import timber.log.Timber;

public class UpdateDeviceIds implements IPostUpdateAction {

    @SuppressLint("ApplySharedPref")
    public void execute(PostUpdateContext context) {
        try {
            SharedPreferences sharedPreferences = context.getContext().getSharedPreferences(DeviceStore.SHARED_PREFERENCES_DEVICE_STORE, Context.MODE_PRIVATE);
            String devices = sharedPreferences.getString(DeviceStore.DEVICES, null);

            class OldDeviceId {
                String uid;
            }

            if (devices != null) {
                Set<OldDeviceId> oldDeviceSet = new Gson().fromJson(devices, new TypeToken<HashSet<OldDeviceId>>() {}.getType());
                Set<DeviceId> newDeviceSet = new HashSet<>();

                for (OldDeviceId oldDeviceId : oldDeviceSet) {
                    try {
                        newDeviceSet.add(new DeviceId(Integer.parseInt(oldDeviceId.uid.split("-")[0]), 1, 5));
                    } catch (Exception e) {
                        Timber.e(e, "Unable to convert old device id: %s", oldDeviceId.uid);
                    }
                }

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(DeviceStore.DEVICES, new Gson().toJson(newDeviceSet));
                editor.commit();
            }
        } catch (Exception e) {
            Timber.e(e, "Unable to convert Device Ids");
        }
    }

}
