package com.valterc.ki2.external;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import timber.log.Timber;

public class ExternalActionManager {

    public static final String ACTION_PROVIDER = "com.valterc.ki2.ACTION_PROVIDER";
    public static final String PREFERENCE_PREFIX = "external:";

    public static final String ACTION_LIST_ACTIONS = "com.valterc.ki2.action.LIST_ACTIONS";
    public static final String ACTION_ACTIONS_RESULT = "com.valterc.ki2.action.ACTIONS_RESULT";
    public static final String ACTION_ACTIONS_CHANGED = "com.valterc.ki2.action.ACTIONS_CHANGED";
    public static final String ACTION_PERFORM = "com.valterc.ki2.action.PERFORM";

    public static final String EXTRA_RESULT_ACTION = "com.valterc.ki2.extra.RESULT_ACTION";
    public static final String EXTRA_REQUEST_ID = "com.valterc.ki2.extra.REQUEST_ID";
    public static final String EXTRA_ACTIONS = "com.valterc.ki2.extra.ACTIONS";
    public static final String EXTRA_ACTION_ID = "com.valterc.ki2.extra.ACTION_ID";
    public static final String EXTRA_DEVICE_ID = "com.valterc.ki2.extra.DEVICE_ID";
    public static final String EXTRA_SWITCH_TYPE = "com.valterc.ki2.extra.SWITCH_TYPE";
    public static final String EXTRA_SWITCH_COMMAND = "com.valterc.ki2.extra.SWITCH_COMMAND";
    public static final String EXTRA_SWITCH_REPEAT = "com.valterc.ki2.extra.SWITCH_REPEAT";
    public static final String EXTRA_TIMESTAMP = "com.valterc.ki2.extra.TIMESTAMP";
    public static final String EXTRA_PROVIDER_PACKAGE = "com.valterc.ki2.extra.PROVIDER_PACKAGE";

    private static final long LIST_ACTIONS_TIMEOUT_MS = 4000;

    private static ExternalActionManager instance;

    private final Context context;
    private final PackageManager packageManager;
    private final Map<ComponentName, ProviderRecord> providers;
    private final List<ExternalActionDescriptor> cachedActions;
    private final Set<Listener> listeners;
    private final Handler mainHandler;
    private boolean started;

    private final BroadcastReceiver resultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                return;
            }
            switch (intent.getAction()) {
                case ACTION_ACTIONS_RESULT:
                    handleActionsResult(intent);
                    break;
                case ACTION_ACTIONS_CHANGED:
                    handleActionsChanged(intent);
                    break;
            }
        }
    };

    private ExternalActionManager(@NonNull Context context) {
        this.context = context.getApplicationContext();
        this.packageManager = this.context.getPackageManager();
        this.providers = new HashMap<>();
        this.cachedActions = new ArrayList<>();
        this.listeners = new HashSet<>();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized ExternalActionManager getInstance(@NonNull Context context) {
        if (instance == null) {
            instance = new ExternalActionManager(context);
        }
        return instance;
    }

    public void start() {
        if (!started) {
            started = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_ACTIONS_RESULT);
            filter.addAction(ACTION_ACTIONS_CHANGED);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(resultReceiver, filter, Context.RECEIVER_EXPORTED);
            } else {
                context.registerReceiver(resultReceiver, filter);
            }
        }
        refreshProviders();
    }

    public void shutdown() {
        if (started) {
            started = false;
            try {
                context.unregisterReceiver(resultReceiver);
            } catch (Exception e) {
                // ignore
            }
        }
        synchronized (providers) {
            providers.clear();
        }
        synchronized (cachedActions) {
            cachedActions.clear();
        }
    }

    public void refreshProviders() {
        List<ComponentName> discovered = discoverProviders();
        Set<ComponentName> discoveredSet = new HashSet<>(discovered);

        List<ComponentName> newProviders = new ArrayList<>();
        synchronized (providers) {
            providers.entrySet().removeIf(entry -> !discoveredSet.contains(entry.getKey()));

            for (ComponentName componentName : discovered) {
                if (!providers.containsKey(componentName)) {
                    ProviderRecord record = new ProviderRecord(componentName);
                    providers.put(componentName, record);
                    newProviders.add(componentName);
                }
            }
        }

        for (ComponentName componentName : newProviders) {
            requestActions(componentName);
        }
    }

    public void addListener(@NonNull Listener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeListener(@NonNull Listener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    @NonNull
    public List<ExternalActionDescriptor> getExternalActionsSnapshot() {
        synchronized (cachedActions) {
            return new ArrayList<>(cachedActions);
        }
    }

    public void performAction(@NonNull ExternalActionTarget target,
                              @NonNull String deviceId,
                              int switchType,
                              int switchCommand,
                              int switchRepeat,
                              long timestamp) {
        synchronized (providers) {
            if (!providers.containsKey(target.getProviderComponent())) {
                Timber.w("External action provider missing: %s", target.getProviderComponent());
                return;
            }
        }

        Intent intent = new Intent(ACTION_PERFORM);
        intent.setPackage(target.getProviderComponent().getPackageName());
        intent.putExtra(EXTRA_ACTION_ID, target.getActionId());
        intent.putExtra(EXTRA_DEVICE_ID, deviceId);
        intent.putExtra(EXTRA_SWITCH_TYPE, switchType);
        intent.putExtra(EXTRA_SWITCH_COMMAND, switchCommand);
        intent.putExtra(EXTRA_SWITCH_REPEAT, switchRepeat);
        intent.putExtra(EXTRA_TIMESTAMP, timestamp);

        context.sendBroadcast(intent);
    }

    public static boolean isExternalPreferenceValue(@Nullable String value) {
        return value != null && value.startsWith(PREFERENCE_PREFIX);
    }

    @Nullable
    public static ExternalActionTarget parsePreferenceValue(@Nullable String value) {
        if (value == null || !value.startsWith(PREFERENCE_PREFIX)) {
            return null;
        }

        String payload = value.substring(PREFERENCE_PREFIX.length());
        int actionSeparator = payload.lastIndexOf('#');
        if (actionSeparator <= 0 || actionSeparator >= payload.length() - 1) {
            return null;
        }

        String componentString = payload.substring(0, actionSeparator);
        String actionId = payload.substring(actionSeparator + 1);
        ComponentName componentName = ComponentName.unflattenFromString(componentString);
        if (componentName == null) {
            return null;
        }

        return new ExternalActionTarget(componentName, actionId);
    }

    @NonNull
    public static String toPreferenceValue(@NonNull ComponentName componentName, @NonNull String actionId) {
        return PREFERENCE_PREFIX + componentName.flattenToString() + "#" + actionId;
    }

    @NonNull
    private List<ComponentName> discoverProviders() {
        Intent intent = new Intent(ACTION_PROVIDER);
        List<ResolveInfo> resolveInfos;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            resolveInfos = packageManager.queryIntentServices(intent, PackageManager.ResolveInfoFlags.of(PackageManager.GET_META_DATA));
        } else {
            resolveInfos = packageManager.queryIntentServices(intent, PackageManager.GET_META_DATA);
        }

        if (resolveInfos == null) {
            return Collections.emptyList();
        }

        List<ComponentName> components = new ArrayList<>();
        for (ResolveInfo info : resolveInfos) {
            if (info.serviceInfo == null || info.serviceInfo.packageName == null || info.serviceInfo.name == null) {
                continue;
            }
            components.add(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));
        }

        return components;
    }

    private void requestActions(@NonNull ComponentName componentName) {
        String requestId = UUID.randomUUID().toString();

        synchronized (providers) {
            ProviderRecord record = providers.get(componentName);
            if (record != null) {
                record.pendingRequestId = requestId;
            }
        }

        Intent intent = new Intent(ACTION_LIST_ACTIONS);
        intent.setPackage(componentName.getPackageName());
        intent.putExtra(EXTRA_RESULT_ACTION, ACTION_ACTIONS_RESULT);
        intent.putExtra(EXTRA_REQUEST_ID, requestId);

        context.sendBroadcast(intent);

        mainHandler.postDelayed(() -> {
            boolean timedOut = false;
            synchronized (providers) {
                ProviderRecord record = providers.get(componentName);
                if (record != null && requestId.equals(record.pendingRequestId)) {
                    record.pendingRequestId = null;
                    timedOut = true;
                }
            }
            if (timedOut) {
                Timber.w("LIST_ACTIONS timed out for %s (request %s)", componentName, requestId);
            }
        }, LIST_ACTIONS_TIMEOUT_MS);
    }

    private void handleActionsResult(@NonNull Intent intent) {
        String requestId = intent.getStringExtra(EXTRA_REQUEST_ID);
        String actionsJson = intent.getStringExtra(EXTRA_ACTIONS);

        if (requestId == null || actionsJson == null) {
            return;
        }

        ComponentName matchedComponent = null;
        synchronized (providers) {
            for (Map.Entry<ComponentName, ProviderRecord> entry : providers.entrySet()) {
                if (requestId.equals(entry.getValue().pendingRequestId)) {
                    matchedComponent = entry.getKey();
                    entry.getValue().pendingRequestId = null;
                    break;
                }
            }
        }

        if (matchedComponent == null) {
            Timber.w("No provider matched request ID: %s", requestId);
            return;
        }

        List<ExternalAction> actions = parseActionsJson(actionsJson);

        synchronized (providers) {
            ProviderRecord record = providers.get(matchedComponent);
            if (record != null) {
                record.actions.clear();
                record.actions.addAll(actions);
            }
        }

        updateCachedActions();
    }

    private void handleActionsChanged(@NonNull Intent intent) {
        String providerPackage = intent.getStringExtra(EXTRA_PROVIDER_PACKAGE);
        if (providerPackage == null) {
            return;
        }

        synchronized (providers) {
            for (Map.Entry<ComponentName, ProviderRecord> entry : providers.entrySet()) {
                if (providerPackage.equals(entry.getKey().getPackageName())) {
                    requestActions(entry.getKey());
                    return;
                }
            }
        }

        Timber.w("ACTIONS_CHANGED from unknown provider: %s", providerPackage);
    }

    @NonNull
    private List<ExternalAction> parseActionsJson(@NonNull String json) {
        List<ExternalAction> actions = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String actionId = obj.getString("action_id");
                String label = obj.getString("label");
                String iconUri = obj.optString("icon_uri", null);
                int allowedSwitches = obj.optInt("allowed_switches", ExternalAction.SWITCH_ALL);
                actions.add(new ExternalAction(actionId, label, iconUri, allowedSwitches));
            }
        } catch (JSONException e) {
            Timber.w(e, "Failed to parse external actions JSON");
        }
        return actions;
    }

    private void updateCachedActions() {
        List<ExternalActionDescriptor> updated = new ArrayList<>();
        synchronized (providers) {
            for (ProviderRecord record : providers.values()) {
                if (record.actions.isEmpty()) {
                    continue;
                }
                for (ExternalAction action : record.actions) {
                    updated.add(new ExternalActionDescriptor(record.componentName, record.appLabel, action));
                }
            }
        }

        synchronized (cachedActions) {
            cachedActions.clear();
            cachedActions.addAll(updated);
        }
        notifyListeners();
    }

    private void notifyListeners() {
        List<Listener> snapshot;
        synchronized (listeners) {
            snapshot = new ArrayList<>(listeners);
        }
        if (snapshot.isEmpty()) {
            return;
        }
        mainHandler.post(() -> {
            for (Listener listener : snapshot) {
                try {
                    listener.onActionsUpdated();
                } catch (Exception e) {
                    Timber.w(e, "External action listener failed");
                }
            }
        });
    }

    private class ProviderRecord {
        final ComponentName componentName;
        final String appLabel;
        final List<ExternalAction> actions;
        String pendingRequestId;

        ProviderRecord(@NonNull ComponentName componentName) {
            this.componentName = componentName;
            this.appLabel = resolveAppLabel(componentName);
            this.actions = new ArrayList<>();
        }
    }

    @NonNull
    private String resolveAppLabel(@NonNull ComponentName componentName) {
        try {
            return packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(componentName.getPackageName(), 0)
            ).toString();
        } catch (Exception e) {
            return componentName.getPackageName();
        }
    }

    public interface Listener {
        void onActionsUpdated();
    }
}
