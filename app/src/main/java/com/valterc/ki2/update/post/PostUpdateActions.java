package com.valterc.ki2.update.post;

import androidx.annotation.NonNull;

import com.valterc.ki2.data.update.PostUpdateActionsStore;
import com.valterc.ki2.update.post.actions.IPostInitPostUpdateAction;
import com.valterc.ki2.update.post.actions.IPostUpdateAction;
import com.valterc.ki2.update.post.actions.IPreInitPostUpdateAction;
import com.valterc.ki2.update.post.actions.KarooAudioAlertsUpdateAction;
import com.valterc.ki2.update.post.actions.KarooBellUpdateAction;
import com.valterc.ki2.update.post.actions.ShowOverlayUpdateAction;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public final class PostUpdateActions {

    private PostUpdateActions() {
    }

    private static final Map<String, IPreInitPostUpdateAction> preInitActionMap;
    private static final Map<String, IPostInitPostUpdateAction> postInitActionMap;

    static {
        preInitActionMap = new HashMap<>();
        preInitActionMap.put(KarooAudioAlertsUpdateAction.class.getSimpleName(), new KarooAudioAlertsUpdateAction());
        preInitActionMap.put(ShowOverlayUpdateAction.class.getSimpleName(), new ShowOverlayUpdateAction());
        preInitActionMap.put(KarooBellUpdateAction.class.getSimpleName(), new KarooBellUpdateAction());
        postInitActionMap = new HashMap<>();
    }

    public static void executePreInit(@NonNull PostUpdateContext context) {
        for (String actionName : preInitActionMap.keySet()) {
            executeAction(context, actionName, preInitActionMap.get(actionName));
        }
    }

    public static void executePostInit(@NonNull PostUpdateContext context) {
        for (String actionName : postInitActionMap.keySet()) {
            executeAction(context, actionName, postInitActionMap.get(actionName));
        }
    }

    private static void executeAction(PostUpdateContext context, String actionName, IPostUpdateAction action) {
        if (actionName == null || action == null) {
            return;
        }

        if (!PostUpdateActionsStore.hasExecuted(context.getContext(), actionName)) {
            Timber.i("Executing post update action: %s", actionName);

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

