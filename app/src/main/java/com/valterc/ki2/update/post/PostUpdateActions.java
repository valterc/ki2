package com.valterc.ki2.update.post;

import com.valterc.ki2.data.update.PostUpdateActionsStore;
import com.valterc.ki2.update.PostUpdateContext;
import com.valterc.ki2.update.post.actions.IPostUpdateAction;
import com.valterc.ki2.update.post.actions.InitializeDevicesPriority;
import com.valterc.ki2.update.post.actions.UpdateDeviceIds;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public final class PostUpdateActions {

    private PostUpdateActions() {
    }

    private static final Map<String, IPostUpdateAction> preInitActionMap;
    private static final Map<String, IPostUpdateAction> postInitActionMap;

    static {
        preInitActionMap = new HashMap<>();
        preInitActionMap.put(UpdateDeviceIds.class.getSimpleName(), new UpdateDeviceIds());

        postInitActionMap = new HashMap<>();
        postInitActionMap.put(InitializeDevicesPriority.class.getSimpleName(), new InitializeDevicesPriority());
    }

    public static void executePreInit(PostUpdateContext context) {
        execute(context, preInitActionMap);
    }

    public static void executePostInit(PostUpdateContext context) {
        execute(context, postInitActionMap);
    }

    private static void execute(PostUpdateContext context, Map<String, IPostUpdateAction> actionMap) {
        for (String actionName : actionMap.keySet()) {
            if (!PostUpdateActionsStore.hasExecuted(context.getContext(), actionName)) {
                Timber.i("Executing post update action: %s", actionName);

                IPostUpdateAction action = postInitActionMap.get(actionName);
                if (action == null) {
                    continue;
                }

                try {
                    action.execute(context);
                    PostUpdateActionsStore.executedAction(context.getContext(), actionName);
                    Timber.i("Executed post update action: %s", actionName);
                } catch (Exception exception) {
                    Timber.e(exception, "Unable to execute post update action: %s", actionName);
                }
            }
        }
    }

}
