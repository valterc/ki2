package com.valterc.ki2.input;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Pair;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.valterc.ki2.R;
import com.valterc.ki2.data.input.KarooKeyEvent;
import com.valterc.ki2.data.input.KeyAction;
import com.valterc.ki2.data.switches.SwitchCommand;
import com.valterc.ki2.data.switches.SwitchCommandType;
import com.valterc.ki2.data.switches.SwitchEvent;
import com.valterc.ki2.data.switches.SwitchType;
import com.valterc.ki2.data.input.KarooKey;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import timber.log.Timber;

public class InputManager {

    /**
     * This map translates a preference value to function able to generate a Switch Key Event from the original switch event.
     */
    private static final Map<String, BiFunction<SwitchEvent, Function<SwitchEvent, KarooKeyEvent>, KarooKeyEvent>> preferenceToSwitchKeyMap = new HashMap<>();

    static {

        /*
         * Any switch event
         */
        preferenceToSwitchKeyMap.put("none", (switchEvent, converter) -> null);

        /*
         *   Single / Double press events
         */
        preferenceToSwitchKeyMap.put("navigate_left", (switchEvent, converter) -> new KarooKeyEvent(KarooKey.LEFT, KeyAction.SINGLE_PRESS, switchEvent.getRepeat()));
        preferenceToSwitchKeyMap.put("navigate_right", (switchEvent, converter) -> new KarooKeyEvent(KarooKey.RIGHT, KeyAction.SINGLE_PRESS, switchEvent.getRepeat()));
        preferenceToSwitchKeyMap.put("pause_resume_confirm", (switchEvent, converter) -> new KarooKeyEvent(KarooKey.CONFIRM, KeyAction.SINGLE_PRESS, switchEvent.getRepeat()));
        preferenceToSwitchKeyMap.put("lap_map_back", (switchEvent, converter) -> new KarooKeyEvent(KarooKey.BACK, KeyAction.SINGLE_PRESS, switchEvent.getRepeat()));
        preferenceToSwitchKeyMap.put("lap", (switchEvent, converter) -> new KarooKeyEvent(KarooKey.BACK, KeyAction.DOUBLE_PRESS, switchEvent.getRepeat()));
        preferenceToSwitchKeyMap.put("press_map_graph_zoom_out", (switchEvent, converter) -> new KarooKeyEvent(KarooKey.LEFT, KeyAction.SIMULATE_LONG_PRESS, switchEvent.getRepeat()));
        preferenceToSwitchKeyMap.put("press_map_graph_zoom_in", (switchEvent, converter) -> new KarooKeyEvent(KarooKey.RIGHT, KeyAction.SIMULATE_LONG_PRESS, switchEvent.getRepeat()));

        /*
         * Double press events
         */
        preferenceToSwitchKeyMap.put("double_press_duplicate_single_press", (switchEvent, converter) ->
                new KarooKeyEvent(converter.apply(new SwitchEvent(switchEvent.getType(), SwitchCommand.SINGLE_CLICK, switchEvent.getRepeat())), 2));

        /*
         *   Hold events
         */
        preferenceToSwitchKeyMap.put("map_graph_zoom_out", (switchEvent, converter) -> new KarooKeyEvent(KarooKey.LEFT, switchEvent.getCommand().getKeyAction(), switchEvent.getRepeat()));
        preferenceToSwitchKeyMap.put("map_graph_zoom_in", (switchEvent, converter) -> new KarooKeyEvent(KarooKey.RIGHT, switchEvent.getCommand().getKeyAction(), switchEvent.getRepeat()));
        preferenceToSwitchKeyMap.put("repeat_single_press", (switchEvent, converter) -> {
            if (switchEvent.getCommand() == SwitchCommand.LONG_PRESS_UP) {
                return null;
            }
            return converter.apply(new SwitchEvent(switchEvent.getType(), SwitchCommand.SINGLE_CLICK, switchEvent.getRepeat()));
        });
        preferenceToSwitchKeyMap.put("hold_short_single_pause_resume_confirm", (switchEvent, converter) -> {
            if (switchEvent.getCommand() != SwitchCommand.LONG_PRESS_DOWN) {
                return null;
            }
            return new KarooKeyEvent(KarooKey.CONFIRM, KeyAction.SINGLE_PRESS, switchEvent.getRepeat());
        });
        preferenceToSwitchKeyMap.put("hold_short_single_lap", (switchEvent, converter) -> {
            if (switchEvent.getCommand() != SwitchCommand.LONG_PRESS_DOWN) {
                return null;
            }
            return new KarooKeyEvent(KarooKey.BACK, KeyAction.DOUBLE_PRESS, switchEvent.getRepeat());
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
    private KarooKeyEvent getKarooKeyEvent(SwitchEvent switchEvent) {
        String preferenceKey = preferenceMap.get(new Pair<>(switchEvent.getType(), switchEvent.getCommand().getCommandType()));
        if (preferenceKey == null) {
            return null;
        }

        String preference = preferences.getString(preferenceKey, null);
        if (preference == null) {
            return null;
        }

        BiFunction<SwitchEvent, Function<SwitchEvent, KarooKeyEvent>, KarooKeyEvent> keyFunction = preferenceToSwitchKeyMap.get(preference);
        if (keyFunction == null) {
            Timber.w("Invalid karoo command from combination, switch: %s, command type: %s", switchEvent.getType(), switchEvent.getCommand().getCommandType());
            return null;
        }

        return keyFunction.apply(switchEvent, this::getKarooKeyEvent);
    }

    @Nullable
    public KarooKeyEvent onSwitch(SwitchEvent switchEvent) {
        if (switchEvent == null) {
            return null;
        }

        return getKarooKeyEvent(switchEvent);
    }

}
