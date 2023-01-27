package com.valterc.ki2.data.update;

import android.content.Context;
import android.content.SharedPreferences;

public final class PostUpdateActionsStore {

    private static final String PREFERENCE_NAME = "PostUpdateActions";

    private PostUpdateActionsStore() {
    }

    /**
     * Indicates if an action has been executed previously.
     *
     * @param context Application context.
     * @param action Action identifier.
     * @return True if the action has been previously executed, False otherwise.
     */
    public static boolean hasExecuted(Context context, String action) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(action, null) != null;
    }

    /**
     * Get the execution result of an action.
     *
     * @param context Application context.
     * @param action Action identifier.
     * @return Action execution result. Can be <c>null</c> if the action has not yet been executed.
     */
    public static String getActionExecutionResult(Context context, String action) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(action, null);
    }

    /**
     * Marks an action has executed. No result will be saved for the action, the current timestamp will be stored as the result.
     *
     * @param context Application context.
     * @param action Action identifier.
     */
    public static void executedAction(Context context, String action) {
        executedAction(context, action, String.valueOf(System.currentTimeMillis()));
    }

    /**
     * Marks an action has executed with a result.
     *
     * @param context Application context.
     * @param action Action identifier.
     * @param result Execution result.
     */
    public static void executedAction(Context context, String action, String result) {
        if (result == null) {
            throw new IllegalArgumentException("Action result must not be null");
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(action, result);
        editor.apply();
    }

}
