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
        preferenceToSwitchKeyMap.put("show_overlay", (switchEvent, converter) -> new KarooKeyEvent(KarooKey.VIRTUAL_SHOW_OVERLAY, KeyAction.SINGLE_PRESS, switchEvent.getRepeat()));

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
        preferenceToSwitchKeyMap.put("press_switch_to_map_page", (switchEvent, converter) -> new KarooKeyEvent(KarooKey.VIRTUAL_SWITCH_TO_MAP_PAGE, KeyAction.SINGLE_PRESS, switchEvent.getRepeat()));
        preferenceToSwitchKeyMap.put("press_take_screenshot", (switchEvent, converter) -> new KarooKeyEvent(KarooKey.VIRTUAL_TAKE_SCREENSHOT, KeyAction.SINGLE_PRESS, switchEvent.getRepeat()));
        preferenceToSwitchKeyMap.put("press_turn_screen_on", (switchEvent, converter) -> new KarooKeyEvent(KarooKey.VIRTUAL_TURN_SCREEN_ON, KeyAction.SINGLE_PRESS, switchEvent.getRepeat()));

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
        preferenceToSwitchKeyMap.put("hold_short_single_switch_to_map_page", (switchEvent, converter) -> {
            if (switchEvent.getCommand() != SwitchCommand.LONG_PRESS_DOWN) {
                return null;
            }
            return new KarooKeyEvent(KarooKey.VIRTUAL_SWITCH_TO_MAP_PAGE, KeyAction.SINGLE_PRESS, switchEvent.getRepeat());
        });
        preferenceToSwitchKeyMap.put("hold_short_single_take_screenshot", (switchEvent, converter) -> {
            if (switchEvent.getCommand() != SwitchCommand.LONG_PRESS_DOWN) {
                return null;
            }
            return new KarooKeyEvent(KarooKey.VIRTUAL_TAKE_SCREENSHOT, KeyAction.SINGLE_PRESS, switchEvent.getRepeat());
        });
        preferenceToSwitchKeyMap.put("hold_short_single_turn_screen_on", (switchEvent, converter) -> {
            if (switchEvent.getCommand() != SwitchCommand.LONG_PRESS_DOWN) {
                return null;
            }
            return new KarooKeyEvent(KarooKey.VIRTUAL_TURN_SCREEN_ON, KeyAction.SINGLE_PRESS, switchEvent.getRepeat());
        });
    }

    private final SharedPreferences preferences;

    /**
     * This map translates physical switch (Left, Right) commands to a preference key.
     */
    private final Map<Pair<SwitchType, SwitchCommandType>, Pair<String, String>> preferenceMap;

    public InputManager(Context context) {
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);

        this.preferenceMap = new HashMap<>();
        this.preferenceMap.put(new Pair<>(SwitchType.D_FLY_CH1, SwitchCommandType.SINGLE_PRESS),
                new Pair<>(context.getString(R.string.preference_switch_ch1_single_press), context.getString(R.string.default_preference_switch_ch1_single_press)));
        this.preferenceMap.put(new Pair<>(SwitchType.D_FLY_CH1, SwitchCommandType.DOUBLE_PRESS),
                new Pair<>(context.getString(R.string.preference_switch_ch1_double_press), context.getString(R.string.default_preference_switch_ch1_double_press)));
        this.preferenceMap.put(new Pair<>(SwitchType.D_FLY_CH1, SwitchCommandType.HOLD),
                new Pair<>(context.getString(R.string.preference_switch_ch1_hold), context.getString(R.string.default_preference_switch_ch1_hold)));

        this.preferenceMap.put(new Pair<>(SwitchType.D_FLY_CH2, SwitchCommandType.SINGLE_PRESS),
                new Pair<>(context.getString(R.string.preference_switch_ch2_single_press), context.getString(R.string.default_preference_switch_ch2_single_press)));
        this.preferenceMap.put(new Pair<>(SwitchType.D_FLY_CH2, SwitchCommandType.DOUBLE_PRESS),
                new Pair<>(context.getString(R.string.preference_switch_ch2_double_press), context.getString(R.string.default_preference_switch_ch2_double_press)));
        this.preferenceMap.put(new Pair<>(SwitchType.D_FLY_CH2, SwitchCommandType.HOLD),
                new Pair<>(context.getString(R.string.preference_switch_ch2_hold), context.getString(R.string.default_preference_switch_ch2_hold)));

        this.preferenceMap.put(new Pair<>(SwitchType.D_FLY_CH3, SwitchCommandType.SINGLE_PRESS),
                new Pair<>(context.getString(R.string.preference_switch_ch3_single_press), context.getString(R.string.default_preference_switch)));
        this.preferenceMap.put(new Pair<>(SwitchType.D_FLY_CH3, SwitchCommandType.DOUBLE_PRESS),
                new Pair<>(context.getString(R.string.preference_switch_ch3_double_press), context.getString(R.string.default_preference_switch)));
        this.preferenceMap.put(new Pair<>(SwitchType.D_FLY_CH3, SwitchCommandType.HOLD),
                new Pair<>(context.getString(R.string.preference_switch_ch3_hold), context.getString(R.string.default_preference_switch)));

        this.preferenceMap.put(new Pair<>(SwitchType.D_FLY_CH4, SwitchCommandType.SINGLE_PRESS),
                new Pair<>(context.getString(R.string.preference_switch_ch4_single_press), context.getString(R.string.default_preference_switch)));
        this.preferenceMap.put(new Pair<>(SwitchType.D_FLY_CH4, SwitchCommandType.DOUBLE_PRESS),
                new Pair<>(context.getString(R.string.preference_switch_ch4_double_press), context.getString(R.string.default_preference_switch)));
        this.preferenceMap.put(new Pair<>(SwitchType.D_FLY_CH4, SwitchCommandType.HOLD),
                new Pair<>(context.getString(R.string.preference_switch_ch4_hold), context.getString(R.string.default_preference_switch)));
    }

    @Nullable
    private KarooKeyEvent getKarooKeyEvent(SwitchEvent switchEvent) {
        Pair<String, String> preferencePair = preferenceMap.get(new Pair<>(switchEvent.getType(), switchEvent.getCommand().getCommandType()));
        if (preferencePair == null) {
            return null;
        }

        String preference = preferences.getString(preferencePair.first, preferencePair.second);
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
