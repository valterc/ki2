package com.valterc.ki2.update;

import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.preferences.device.DevicePreferences;
import com.valterc.ki2.data.update.PostUpdateActionsStore;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import timber.log.Timber;

public final class PostUpdateActions {

    private PostUpdateActions() {
    }

    private static final Map<String, Consumer<PostUpdateContext>> actionMap;

    static {
        actionMap = new HashMap<>();
        actionMap.put("InitializeDevicesPriority", c -> {
            int priority = 0;
            for (DeviceId deviceId: c.getDeviceStore().getDevices().stream().sorted(Comparator.comparing(DeviceId::getName)).collect(Collectors.toList())) {
                new DevicePreferences(c.getContext(), deviceId).setPriority(priority++);
            }
        });
    }

    public static void execute(PostUpdateContext context) {
        for (String action: actionMap.keySet()) {
            if (!PostUpdateActionsStore.hasExecuted(context.getContext(), action)) {
                Timber.i("Executing post update action: %s", action);

                Consumer<PostUpdateContext> actionConsumer = actionMap.get(action);
                if (actionConsumer == null) {
                    continue;
                }

                try {
                    actionConsumer.accept(context);
                    PostUpdateActionsStore.executedAction(context.getContext(), action);
                    Timber.i("Executed post update action: %s", action);
                } catch (Exception exception) {
                    Timber.e(exception, "Unable to execute post update action: %s", action);
                }
            }
        }
    }

}
