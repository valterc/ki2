package com.valterc.ki2.input;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Pair;

import androidx.preference.PreferenceManager;

import com.valterc.ki2.R;
import com.valterc.ki2.data.switches.SwitchCommand;
import com.valterc.ki2.data.switches.SwitchCommandType;
import com.valterc.ki2.data.switches.SwitchEvent;
import com.valterc.ki2.data.switches.SwitchKeyEvent;
import com.valterc.ki2.data.switches.SwitchType;
import com.valterc.ki2.karoo.input.KarooKey;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public class InputManager {

    private static final Map<String, Pair<KarooKey, SwitchCommandType>> stringValueMap = new HashMap<>();

    static {
        stringValueMap.put("none", new Pair<>(KarooKey.NONE, SwitchCommandType.NONE));
        stringValueMap.put("navigate_left", new Pair<>(KarooKey.LEFT, SwitchCommandType.SINGLE_PRESS));
        stringValueMap.put("navigate_right", new Pair<>(KarooKey.RIGHT, SwitchCommandType.SINGLE_PRESS));
        stringValueMap.put("pause_resume_confirm", new Pair<>(KarooKey.CONFIRM, SwitchCommandType.SINGLE_PRESS));
        stringValueMap.put("lap_map_back", new Pair<>(KarooKey.BACK, SwitchCommandType.SINGLE_PRESS));
        stringValueMap.put("lap", new Pair<>(KarooKey.BACK, SwitchCommandType.DOUBLE_PRESS));
        stringValueMap.put("map_graph_zoom_out", new Pair<>(KarooKey.LEFT, SwitchCommandType.HOLD));
        stringValueMap.put("map_graph_zoom_in", new Pair<>(KarooKey.RIGHT, SwitchCommandType.HOLD));
    }

    private final Context context;
    private final InputAdapter inputAdapter;
    private final SharedPreferences preferences;

    private final Map<Pair<SwitchType, SwitchCommandType>, String> preferenceMap;

    public InputManager(Context context) {
        this.context = context;
        this.inputAdapter = new InputAdapter(context);
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);

        this.preferenceMap = new HashMap<>();
        this.preferenceMap.put(new Pair<>(SwitchType.LEFT, SwitchCommandType.SINGLE_PRESS), context.getString(R.string.preference_left_switch_single_press));
        this.preferenceMap.put(new Pair<>(SwitchType.LEFT, SwitchCommandType.DOUBLE_PRESS), context.getString(R.string.preference_left_switch_double_press));
        this.preferenceMap.put(new Pair<>(SwitchType.LEFT, SwitchCommandType.HOLD), context.getString(R.string.preference_left_switch_hold));
        this.preferenceMap.put(new Pair<>(SwitchType.RIGHT, SwitchCommandType.SINGLE_PRESS), context.getString(R.string.preference_right_switch_single_press));
        this.preferenceMap.put(new Pair<>(SwitchType.RIGHT, SwitchCommandType.DOUBLE_PRESS), context.getString(R.string.preference_right_switch_double_press));
        this.preferenceMap.put(new Pair<>(SwitchType.RIGHT, SwitchCommandType.HOLD), context.getString(R.string.preference_right_switch_hold));
    }

    private SwitchKeyEvent convertSwitchToSwitchKey(SwitchEvent switchEvent) {

        String preferenceKey = preferenceMap.get(new Pair<>(switchEvent.getType(), switchEvent.getCommand().getCommandType()));
        if (preferenceKey == null) {
            return null;
        }

        String preference = preferences.getString(preferenceKey, null);
        if (preference == null) {
            return null;
        }

        Pair<KarooKey, SwitchCommandType> karooCommand = stringValueMap.get(preference);
        if (karooCommand == null) {
            Timber.w("Invalid karoo command from combination, switch: %s, command type: %s", switchEvent.getType(), switchEvent.getCommand().getCommandType());
            return null;
        }

        return getSwitchKeyEvent(switchEvent, karooCommand);
    }

    private SwitchKeyEvent getSwitchKeyEvent(SwitchEvent switchEvent, Pair<KarooKey, SwitchCommandType> karooCommand) {
        if (karooCommand.first == KarooKey.NONE || karooCommand.second == SwitchCommandType.NONE) {
            return null;
        }

        if (karooCommand.second == SwitchCommandType.HOLD) {
            return new SwitchKeyEvent(karooCommand.first, switchEvent.getCommand(), switchEvent.getRepeat());
        } else {
            return new SwitchKeyEvent(karooCommand.first, karooCommand.second.getCommand(), switchEvent.getRepeat());
        }
    }

    public SwitchKeyEvent onSwitch(SwitchEvent switchEvent) {

        if (switchEvent == null) {
            return null;
        }

        return convertSwitchToSwitchKey(switchEvent);
    }

}
