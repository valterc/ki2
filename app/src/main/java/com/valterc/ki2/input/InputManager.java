package com.valterc.ki2.input;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Pair;

import androidx.annotation.Nullable;
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
import java.util.function.BiFunction;
import java.util.function.Function;

import timber.log.Timber;

public class InputManager {

    /**
     * This map translates a preference value to function able to generate a Switch Key Event from the original switch event.
     */
    private static final Map<String, BiFunction<SwitchEvent, Function<SwitchEvent, SwitchKeyEvent>,  SwitchKeyEvent>> preferenceToSwitchKeyMap = new HashMap<>();

    static {

        /*
         * Any switch event
         */
        preferenceToSwitchKeyMap.put("none", (switchEvent, converter) -> null);

        /*
         *   Single / Double press events
         */
        preferenceToSwitchKeyMap.put("navigate_left", (switchEvent, converter) -> new SwitchKeyEvent(KarooKey.LEFT, SwitchCommand.SINGLE_CLICK, switchEvent.getRepeat()));
        preferenceToSwitchKeyMap.put("navigate_right", (switchEvent, converter) -> new SwitchKeyEvent(KarooKey.RIGHT, SwitchCommand.SINGLE_CLICK, switchEvent.getRepeat()));
        preferenceToSwitchKeyMap.put("pause_resume_confirm", (switchEvent, converter) -> new SwitchKeyEvent(KarooKey.CONFIRM, SwitchCommand.SINGLE_CLICK, switchEvent.getRepeat()));
        preferenceToSwitchKeyMap.put("lap_map_back", (switchEvent, converter) -> new SwitchKeyEvent(KarooKey.BACK, SwitchCommand.SINGLE_CLICK, switchEvent.getRepeat()));
        preferenceToSwitchKeyMap.put("lap", (switchEvent, converter) -> new SwitchKeyEvent(KarooKey.BACK, SwitchCommand.DOUBLE_CLICK, switchEvent.getRepeat()));

        /*
         *   Hold events
         */
        preferenceToSwitchKeyMap.put("map_graph_zoom_out", (switchEvent, converter) -> new SwitchKeyEvent(KarooKey.LEFT, switchEvent.getCommand(), switchEvent.getRepeat()));
        preferenceToSwitchKeyMap.put("map_graph_zoom_in", (switchEvent, converter) -> new SwitchKeyEvent(KarooKey.RIGHT, switchEvent.getCommand(), switchEvent.getRepeat()));
        preferenceToSwitchKeyMap.put("repeat_single_press", (switchEvent, converter) -> {
            if (switchEvent.getCommand() == SwitchCommand.LONG_PRESS_UP)
            {
                return null;
            }

            return converter.apply(new SwitchEvent(switchEvent.getType(), SwitchCommand.SINGLE_CLICK, switchEvent.getRepeat()));
        });
    }

    private final SharedPreferences preferences;

    /**
     * This map translates physical switch (Left, Right) commands to a preference key.
     */
    private final Map<Pair<SwitchType, SwitchCommandType>, String> preferenceMap;

    public InputManager(Context context) {
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);

        this.preferenceMap = new HashMap<>();
        this.preferenceMap.put(new Pair<>(SwitchType.LEFT, SwitchCommandType.SINGLE_PRESS), context.getString(R.string.preference_left_switch_single_press));
        this.preferenceMap.put(new Pair<>(SwitchType.LEFT, SwitchCommandType.DOUBLE_PRESS), context.getString(R.string.preference_left_switch_double_press));
        this.preferenceMap.put(new Pair<>(SwitchType.LEFT, SwitchCommandType.HOLD), context.getString(R.string.preference_left_switch_hold));
        this.preferenceMap.put(new Pair<>(SwitchType.RIGHT, SwitchCommandType.SINGLE_PRESS), context.getString(R.string.preference_right_switch_single_press));
        this.preferenceMap.put(new Pair<>(SwitchType.RIGHT, SwitchCommandType.DOUBLE_PRESS), context.getString(R.string.preference_right_switch_double_press));
        this.preferenceMap.put(new Pair<>(SwitchType.RIGHT, SwitchCommandType.HOLD), context.getString(R.string.preference_right_switch_hold));
    }

    @Nullable
    private SwitchKeyEvent getSwitchKeyEvent(SwitchEvent switchEvent) {
        String preferenceKey = preferenceMap.get(new Pair<>(switchEvent.getType(), switchEvent.getCommand().getCommandType()));
        if (preferenceKey == null) {
            return null;
        }

        String preference = preferences.getString(preferenceKey, null);
        if (preference == null) {
            return null;
        }

        BiFunction<SwitchEvent, Function<SwitchEvent, SwitchKeyEvent>, SwitchKeyEvent> keyFunction = preferenceToSwitchKeyMap.get(preference);
        if (keyFunction == null) {
            Timber.w("Invalid karoo command from combination, switch: %s, command type: %s", switchEvent.getType(), switchEvent.getCommand().getCommandType());
            return null;
        }

        return keyFunction.apply(switchEvent, this::getSwitchKeyEvent);
    }

    @Nullable
    public SwitchKeyEvent onSwitch(SwitchEvent switchEvent) {
        if (switchEvent == null) {
            return null;
        }

        return getSwitchKeyEvent(switchEvent);
    }

}
