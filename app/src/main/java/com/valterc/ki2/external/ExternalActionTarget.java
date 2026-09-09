package com.valterc.ki2.external;

import android.content.ComponentName;

import androidx.annotation.NonNull;

public class ExternalActionTarget {

    @NonNull
    private final ComponentName providerComponent;
    @NonNull
    private final String actionId;

    public ExternalActionTarget(@NonNull ComponentName providerComponent, @NonNull String actionId) {
        this.providerComponent = providerComponent;
        this.actionId = actionId;
    }

    @NonNull
    public ComponentName getProviderComponent() {
        return providerComponent;
    }

    @NonNull
    public String getActionId() {
        return actionId;
    }
}
