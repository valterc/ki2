package com.valterc.ki2.update.post.actions;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.device.DeviceStore;
import com.valterc.ki2.data.device.DeviceType;
import com.valterc.ki2.update.post.PostUpdateContext;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import timber.log.Timber;

public class UpdateDeviceIds implements IPreInitPostUpdateAction {

    @SuppressLint("ApplySharedPref")
    public void execute(PostUpdateContext context) {
        try {
            SharedPreferences sharedPreferences = context.getContext().getSharedPreferences(DeviceStore.SHARED_PREFERENCES_DEVICE_STORE, Context.MODE_PRIVATE);
            String devices = sharedPreferences.getString(DeviceStore.DEVICES, null);

            if (devices != null) {
                Set<OldDeviceId> oldDeviceSet = new Gson().fromJson(devices, new TypeToken<HashSet<OldDeviceId>>() {}.getType());
                if (oldDeviceSet == null || oldDeviceSet.isEmpty() || oldDeviceSet.stream().allMatch(Objects::isNull)) {
                    Timber.i("Did not convert Device Ids, original set was empty or invalid");
                    return;
                }

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

    @SuppressWarnings("unused")
    private static class OldDeviceId {
        private final String uid;

        private final DeviceType deviceType;

        public OldDeviceId(String uid, DeviceType deviceType) {
            this.uid = uid;
            this.deviceType = deviceType;
        }

        public String getUid() {
            return uid;
        }

        public DeviceType getDeviceType() {
            return deviceType;
        }
    }

}
