package com.valterc.ki2.external;

import android.content.ComponentName;

import androidx.annotation.NonNull;

public class ExternalActionDescriptor {

    @NonNull
    private final ComponentName providerComponent;
    @NonNull
    private final String appLabel;
    @NonNull
    private final ExternalAction action;

    public ExternalActionDescriptor(@NonNull ComponentName providerComponent,
                                    @NonNull String appLabel,
                                    @NonNull ExternalAction action) {
        this.providerComponent = providerComponent;
        this.appLabel = appLabel;
        this.action = action;
    }

    @NonNull
    public ComponentName getProviderComponent() {
        return providerComponent;
    }

    @NonNull
    public String getAppLabel() {
        return appLabel;
    }

    @NonNull
    public ExternalAction getAction() {
        return action;
    }
}
