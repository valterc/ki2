package com.valterc.ki2.input;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Pair;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.valterc.ki2.R;
import com.valterc.ki2.data.action.KarooActionEvent;
import com.valterc.ki2.data.switches.SwitchCommand;
import com.valterc.ki2.data.switches.SwitchCommandType;
import com.valterc.ki2.data.switches.SwitchEvent;
import com.valterc.ki2.data.switches.SwitchType;
import com.valterc.ki2.data.action.KarooAction;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import timber.log.Timber;

public class InputManager {

    /**
     * This map translates a preference value to function able to generate a Karoo action event from the original switch event.
     */
    private static final Map<String, BiFunction<SwitchEvent, Function<SwitchEvent, KarooActionEvent>, KarooActionEvent>> preferenceToSwitchKeyMap = new HashMap<>();

    static {

        /*
         * Any switch event
         */
        preferenceToSwitchKeyMap.put("none", (switchEvent, converter) -> null);
        preferenceToSwitchKeyMap.put("show_overlay", (switchEvent, converter) -> new KarooActionEvent(KarooAction.VIRTUAL_SHOW_OVERLAY, switchEvent.getRepeat()));

        /*
         *   Single / Double press events
         */
        preferenceToSwitchKeyMap.put("top_left", (switchEvent, converter) -> new KarooActionEvent(KarooAction.TOP_LEFT, switchEvent.getRepeat()));
        preferenceToSwitchKeyMap.put("top_right", (switchEvent, converter) -> new KarooActionEvent(KarooAction.TOP_RIGHT, switchEvent.getRepeat()));
        preferenceToSwitchKeyMap.put("bottom_left", (switchEvent, converter) -> new KarooActionEvent(KarooAction.BOTTOM_LEFT,switchEvent.getRepeat()));
        preferenceToSwitchKeyMap.put("bottom_right", (switchEvent, converter) -> new KarooActionEvent(KarooAction.BOTTOM_RIGHT, switchEvent.getRepeat()));
        preferenceToSwitchKeyMap.put("lap", (switchEvent, converter) -> new KarooActionEvent(KarooAction.VIRTUAL_LAP, switchEvent.getRepeat()));
        preferenceToSwitchKeyMap.put("press_map_graph_zoom_out", (switchEvent, converter) -> new KarooActionEvent(KarooAction.VIRTUAL_ZOOM_OUT, switchEvent.getRepeat()));
        preferenceToSwitchKeyMap.put("press_map_graph_zoom_in", (switchEvent, converter) -> new KarooActionEvent(KarooAction.VIRTUAL_ZOOM_IN, switchEvent.getRepeat()));
        preferenceToSwitchKeyMap.put("press_switch_to_map_page", (switchEvent, converter) -> new KarooActionEvent(KarooAction.VIRTUAL_SWITCH_TO_MAP_PAGE, switchEvent.getRepeat()));
        preferenceToSwitchKeyMap.put("press_turn_screen_on", (switchEvent, converter) -> new KarooActionEvent(KarooAction.VIRTUAL_TURN_SCREEN_ON, switchEvent.getRepeat()));
        preferenceToSwitchKeyMap.put("press_turn_screen_off", (switchEvent, converter) -> new KarooActionEvent(KarooAction.VIRTUAL_TURN_SCREEN_OFF, switchEvent.getRepeat()));
        preferenceToSwitchKeyMap.put("press_toggle_audio_alerts", (switchEvent, converter) -> new KarooActionEvent(KarooAction.VIRTUAL_TOGGLE_AUDIO_ALERTS, switchEvent.getRepeat()));
        preferenceToSwitchKeyMap.put("press_disable_audio_alerts", (switchEvent, converter) -> new KarooActionEvent(KarooAction.VIRTUAL_DISABLE_AUDIO_ALERTS, switchEvent.getRepeat()));
        preferenceToSwitchKeyMap.put("press_enable_audio_alerts", (switchEvent, converter) -> new KarooActionEvent(KarooAction.VIRTUAL_ENABLE_AUDIO_ALERTS, switchEvent.getRepeat()));
        preferenceToSwitchKeyMap.put("press_single_beep", (switchEvent, converter) -> new KarooActionEvent(KarooAction.VIRTUAL_SINGLE_BEEP, switchEvent.getRepeat()));
        preferenceToSwitchKeyMap.put("press_double_beep", (switchEvent, converter) -> new KarooActionEvent(KarooAction.VIRTUAL_DOUBLE_BEEP, switchEvent.getRepeat()));
        preferenceToSwitchKeyMap.put("press_bell", (switchEvent, converter) -> new KarooActionEvent(KarooAction.VIRTUAL_BELL, switchEvent.getRepeat()));
        preferenceToSwitchKeyMap.put("press_control_center", (switchEvent, converter) -> new KarooActionEvent(KarooAction.VIRTUAL_CONTROL_CENTER, switchEvent.getRepeat()));

        /*
         * Double press events
         */
        preferenceToSwitchKeyMap.put("double_press_duplicate_single_press", (switchEvent, converter) ->
                new KarooActionEvent(converter.apply(new SwitchEvent(switchEvent.getType(), SwitchCommand.SINGLE_CLICK, switchEvent.getRepeat())), 2));

        /*
         *   Hold events
         */
        preferenceToSwitchKeyMap.put("map_graph_zoom_out", (switchEvent, converter) -> {
            if (switchEvent.getCommand() == SwitchCommand.LONG_PRESS_UP) {
                return null;
            }

            return new KarooActionEvent(KarooAction.VIRTUAL_ZOOM_OUT, 0);
        });
        preferenceToSwitchKeyMap.put("map_graph_zoom_in", (switchEvent, converter) -> {
            if (switchEvent.getCommand() == SwitchCommand.LONG_PRESS_UP) {
                return null;
            }

            return new KarooActionEvent(KarooAction.VIRTUAL_ZOOM_IN, 0);
        });
        preferenceToSwitchKeyMap.put("hold_short_single_map_graph_zoom_out", (switchEvent, converter) -> {
            if (switchEvent.getCommand() != SwitchCommand.LONG_PRESS_DOWN) {
                return null;
            }

            return new KarooActionEvent(KarooAction.VIRTUAL_ZOOM_OUT, 0);
        });
        preferenceToSwitchKeyMap.put("hold_short_single_map_graph_zoom_in", (switchEvent, converter) -> {
            if (switchEvent.getCommand() != SwitchCommand.LONG_PRESS_DOWN) {
                return null;
            }

            return new KarooActionEvent(KarooAction.VIRTUAL_ZOOM_IN, 0);
        });
        preferenceToSwitchKeyMap.put("hold_short_single_control_center", (switchEvent, converter) -> {
            if (switchEvent.getCommand() != SwitchCommand.LONG_PRESS_DOWN) {
                return null;
            }

            return new KarooActionEvent(KarooAction.VIRTUAL_CONTROL_CENTER, 0);
        });
        preferenceToSwitchKeyMap.put("map_graph_slow_zoom_out", (switchEvent, converter) -> {
            if (switchEvent.getCommand() == SwitchCommand.LONG_PRESS_UP ||
                    (switchEvent.getCommand() == SwitchCommand.LONG_PRESS_CONTINUE && switchEvent.getRepeat() % 2 != 0)) {
                return null;
            }

            return new KarooActionEvent(KarooAction.VIRTUAL_ZOOM_OUT, 0);
        });
        preferenceToSwitchKeyMap.put("map_graph_slow_zoom_in", (switchEvent, converter) -> {
            if (switchEvent.getCommand() == SwitchCommand.LONG_PRESS_UP ||
                    (switchEvent.getCommand() == SwitchCommand.LONG_PRESS_CONTINUE && switchEvent.getRepeat() % 2 != 0)) {
                return null;
            }

            return new KarooActionEvent(KarooAction.VIRTUAL_ZOOM_IN, 0);
        });
        preferenceToSwitchKeyMap.put("repeat_single_press", (switchEvent, converter) -> {
            if (switchEvent.getCommand() == SwitchCommand.LONG_PRESS_UP) {
                return null;
            }
            return converter.apply(new SwitchEvent(switchEvent.getType(), SwitchCommand.SINGLE_CLICK, switchEvent.getRepeat()));
        });
        preferenceToSwitchKeyMap.put("hold_short_single_bottom_left", (switchEvent, converter) -> {
            if (switchEvent.getCommand() != SwitchCommand.LONG_PRESS_DOWN) {
                return null;
            }
            return new KarooActionEvent(KarooAction.BOTTOM_LEFT, switchEvent.getRepeat());
        });
        preferenceToSwitchKeyMap.put("hold_short_single_bottom_right", (switchEvent, converter) -> {
            if (switchEvent.getCommand() != SwitchCommand.LONG_PRESS_DOWN) {
                return null;
            }
            return new KarooActionEvent(KarooAction.BOTTOM_RIGHT, switchEvent.getRepeat());
        });
        preferenceToSwitchKeyMap.put("hold_short_single_lap", (switchEvent, converter) -> {
            if (switchEvent.getCommand() != SwitchCommand.LONG_PRESS_DOWN) {
                return null;
            }
            return new KarooActionEvent(KarooAction.VIRTUAL_LAP, switchEvent.getRepeat());
        });
        preferenceToSwitchKeyMap.put("hold_short_single_switch_to_map_page", (switchEvent, converter) -> {
            if (switchEvent.getCommand() != SwitchCommand.LONG_PRESS_DOWN) {
                return null;
            }
            return new KarooActionEvent(KarooAction.VIRTUAL_SWITCH_TO_MAP_PAGE, switchEvent.getRepeat());
        });
        preferenceToSwitchKeyMap.put("hold_short_single_turn_screen_on", (switchEvent, converter) -> {
            if (switchEvent.getCommand() != SwitchCommand.LONG_PRESS_DOWN) {
                return null;
            }
            return new KarooActionEvent(KarooAction.VIRTUAL_TURN_SCREEN_ON, switchEvent.getRepeat());
        });
        preferenceToSwitchKeyMap.put("hold_short_single_toggle_audio_alerts", (switchEvent, converter) -> {
            if (switchEvent.getCommand() != SwitchCommand.LONG_PRESS_DOWN) {
                return null;
            }
            return new KarooActionEvent(KarooAction.VIRTUAL_TOGGLE_AUDIO_ALERTS, switchEvent.getRepeat());
        });
        preferenceToSwitchKeyMap.put("hold_short_single_disable_audio_alerts", (switchEvent, converter) -> {
            if (switchEvent.getCommand() != SwitchCommand.LONG_PRESS_DOWN) {
                return null;
            }
            return new KarooActionEvent(KarooAction.VIRTUAL_DISABLE_AUDIO_ALERTS, switchEvent.getRepeat());
        });
        preferenceToSwitchKeyMap.put("hold_short_single_enable_audio_alerts", (switchEvent, converter) -> {
            if (switchEvent.getCommand() != SwitchCommand.LONG_PRESS_DOWN) {
                return null;
            }
            return new KarooActionEvent(KarooAction.VIRTUAL_ENABLE_AUDIO_ALERTS, switchEvent.getRepeat());
        });
        preferenceToSwitchKeyMap.put("hold_short_single_single_beep", (switchEvent, converter) -> {
            if (switchEvent.getCommand() != SwitchCommand.LONG_PRESS_DOWN) {
                return null;
            }
            return new KarooActionEvent(KarooAction.VIRTUAL_SINGLE_BEEP, switchEvent.getRepeat());
        });
        preferenceToSwitchKeyMap.put("hold_short_single_double_beep", (switchEvent, converter) -> {
            if (switchEvent.getCommand() != SwitchCommand.LONG_PRESS_DOWN) {
                return null;
            }
            return new KarooActionEvent(KarooAction.VIRTUAL_DOUBLE_BEEP, switchEvent.getRepeat());
        });
        preferenceToSwitchKeyMap.put("hold_continuous_single_beep", (switchEvent, converter) -> {
            if (switchEvent.getCommand() == SwitchCommand.LONG_PRESS_UP) {
                return null;
            }
            return new KarooActionEvent(KarooAction.VIRTUAL_SINGLE_BEEP, switchEvent.getRepeat());
        });
        preferenceToSwitchKeyMap.put("hold_short_single_bell", (switchEvent, converter) -> {
            if (switchEvent.getCommand() != SwitchCommand.LONG_PRESS_DOWN) {
                return null;
            }
            return new KarooActionEvent(KarooAction.VIRTUAL_BELL, switchEvent.getRepeat());
        });
        preferenceToSwitchKeyMap.put("hold_continuous_bell", (switchEvent, converter) -> {
            if (switchEvent.getCommand() == SwitchCommand.LONG_PRESS_UP) {
                return null;
            }
            return new KarooActionEvent(KarooAction.VIRTUAL_BELL, switchEvent.getRepeat());
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
    private KarooActionEvent getKarooActionEvent(SwitchEvent switchEvent) {
        Pair<String, String> preferencePair = preferenceMap.get(new Pair<>(switchEvent.getType(), switchEvent.getCommand().getCommandType()));
        if (preferencePair == null) {
            return null;
        }

        String preference = preferences.getString(preferencePair.first, preferencePair.second);
        if (preference == null) {
            return null;
        }

        BiFunction<SwitchEvent, Function<SwitchEvent, KarooActionEvent>, KarooActionEvent> keyFunction = preferenceToSwitchKeyMap.get(preference);
        if (keyFunction == null) {
            Timber.w("Invalid karoo command from combination, switch: %s, command type: %s", switchEvent.getType(), switchEvent.getCommand().getCommandType());
            return null;
        }

        return keyFunction.apply(switchEvent, this::getKarooActionEvent);
    }

    @Nullable
    public KarooActionEvent onSwitch(SwitchEvent switchEvent) {
        if (switchEvent == null) {
            return null;
        }

        return getKarooActionEvent(switchEvent);
    }

}
