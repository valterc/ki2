package com.valterc.ki2.sampleprovider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SampleActionProviderReceiver extends BroadcastReceiver {

    private static final String TAG = "SampleActionProvider";

    private static final String ACTION_LIST_ACTIONS = "com.valterc.ki2.action.LIST_ACTIONS";
    private static final String ACTION_ACTIONS_CHANGED = "com.valterc.ki2.action.ACTIONS_CHANGED";
    private static final String ACTION_PERFORM = "com.valterc.ki2.action.PERFORM";

    private static final String EXTRA_RESULT_ACTION = "com.valterc.ki2.extra.RESULT_ACTION";
    private static final String EXTRA_REQUEST_ID = "com.valterc.ki2.extra.REQUEST_ID";
    private static final String EXTRA_ACTIONS = "com.valterc.ki2.extra.ACTIONS";
    private static final String EXTRA_PROVIDER_PACKAGE = "com.valterc.ki2.extra.PROVIDER_PACKAGE";
    private static final String EXTRA_ACTION_ID = "com.valterc.ki2.extra.ACTION_ID";
    private static final String EXTRA_DEVICE_ID = "com.valterc.ki2.extra.DEVICE_ID";
    private static final String EXTRA_SWITCH_TYPE = "com.valterc.ki2.extra.SWITCH_TYPE";
    private static final String EXTRA_SWITCH_COMMAND = "com.valterc.ki2.extra.SWITCH_COMMAND";
    private static final String EXTRA_SWITCH_REPEAT = "com.valterc.ki2.extra.SWITCH_REPEAT";
    private static final String EXTRA_TIMESTAMP = "com.valterc.ki2.extra.TIMESTAMP";

    private static final int SWITCH_CH1 = 1;
    private static final int SWITCH_ALL = 15;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        switch (intent.getAction()) {
            case ACTION_LIST_ACTIONS:
                handleListActions(context, intent);
                break;
            case ACTION_PERFORM:
                handlePerformAction(intent);
                break;
        }
    }

    private void handleListActions(Context context, Intent intent) {
        String resultAction = intent.getStringExtra(EXTRA_RESULT_ACTION);
        String requestId = intent.getStringExtra(EXTRA_REQUEST_ID);
        if (resultAction == null || requestId == null) {
            return;
        }

        try {
            JSONArray actions = new JSONArray();

            JSONObject toggle = new JSONObject();
            toggle.put("action_id", "sample_toggle");
            toggle.put("label", "Sample Toggle");
            toggle.put("icon_uri", JSONObject.NULL);
            toggle.put("allowed_switches", SWITCH_ALL);
            actions.put(toggle);

            JSONObject ch1Only = new JSONObject();
            ch1Only.put("action_id", "ch1_only");
            ch1Only.put("label", "CH1 Only Action");
            ch1Only.put("icon_uri", JSONObject.NULL);
            ch1Only.put("allowed_switches", SWITCH_CH1);
            actions.put(ch1Only);

            Intent response = new Intent(resultAction);
            response.setPackage("com.valterc.ki2");
            response.putExtra(EXTRA_REQUEST_ID, requestId);
            response.putExtra(EXTRA_ACTIONS, actions.toString());
            context.sendBroadcast(response);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to build actions JSON", e);
        }
    }

    private void handlePerformAction(Intent intent) {
        String actionId = intent.getStringExtra(EXTRA_ACTION_ID);
        String deviceId = intent.getStringExtra(EXTRA_DEVICE_ID);
        int switchType = intent.getIntExtra(EXTRA_SWITCH_TYPE, 0);
        int switchCommand = intent.getIntExtra(EXTRA_SWITCH_COMMAND, 0);
        int switchRepeat = intent.getIntExtra(EXTRA_SWITCH_REPEAT, 0);
        long timestamp = intent.getLongExtra(EXTRA_TIMESTAMP, 0);

        Log.i(TAG, "performAction: " + actionId
                + " device=" + deviceId
                + " switchType=" + switchType
                + " switchCommand=" + switchCommand
                + " switchRepeat=" + switchRepeat
                + " timestamp=" + timestamp);
    }

    /**
     * Notify Ki2 that this provider's available actions have changed.
     * Call this when actions are added, removed, or modified (e.g. after a
     * configuration change) so Ki2 re-queries the action list.
     */
    public static void notifyActionsChanged(Context context) {
        Intent intent = new Intent(ACTION_ACTIONS_CHANGED);
        intent.setPackage("com.valterc.ki2");
        intent.putExtra(EXTRA_PROVIDER_PACKAGE, context.getPackageName());
        context.sendBroadcast(intent);
    }
}
