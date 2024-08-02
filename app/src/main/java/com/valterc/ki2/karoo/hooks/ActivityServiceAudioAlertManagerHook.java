package com.valterc.ki2.karoo.hooks;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Parcelable;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.annotation.Nullable;

import com.valterc.ki2.utils.function.ThrowingBiConsumer;
import com.valterc.ki2.utils.function.ThrowingConsumer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import io.hammerhead.sdk.v0.SdkContext;
import kotlin.Lazy;
import kotlin.LazyKt;

@SuppressWarnings({"unchecked", "rawtypes", "SameParameterValue", "UnusedReturnValue"})
@SuppressLint("LogNotTimber")
public class ActivityServiceAudioAlertManagerHook {

    private ActivityServiceAudioAlertManagerHook() {
    }

    private static final Lazy<Class<? extends Enum>> TYPE_BEEP_PATTERN = LazyKt.lazy(() -> {
        try {
            return (Class<? extends Enum>) Class.forName("io.hammerhead.audioalertmodule.beeper.BeepPattern");
        } catch (Exception e) {
            Log.w("KI2", "Unable to get initMethod pattern type", e);
        }

        return null;
    });

    private static final Lazy<Method> METHOD_BEEP_PATTERN_GET_STEPS = LazyKt.lazy(() -> {
        try {
            Class<? extends Enum> type = TYPE_BEEP_PATTERN.getValue();
            Optional<Method> optionalMethod = Arrays.stream(type.getDeclaredMethods()).filter(m -> List.class.isAssignableFrom(m.getReturnType())).findFirst();

            if (!optionalMethod.isPresent()) {
                throw new Exception("Unable to find get steps method");
            }

            return optionalMethod.get();
        } catch (Exception e) {
            Log.w("KI2", "Unable to get get steps type", e);
        }

        return null;
    });

    private static final Lazy<Class<?>> TYPE_AUDIO_ALERT_TONE_1 = LazyKt.lazy(() -> {
        try {
            return Class.forName("io.hammerhead.datamodels.profiles.AudioAlertTone");
        } catch (Exception e) {
            Log.w("KI2", "Unable to get AudioAlertTone via method 1", e);
        }

        return null;
    });

    private static final Lazy<Class<?>> TYPE_AUDIO_ALERT_TONE_2 = LazyKt.lazy(() -> {
        try {
            Class<? extends Enum> type = TYPE_BEEP_PATTERN.getValue();
            Enum[] enumConstants = type.getEnumConstants();
            for (Enum enumValue : Objects.requireNonNull(enumConstants)) {
                List<?> listStep = (List<?>) METHOD_BEEP_PATTERN_GET_STEPS.getValue().invoke(enumValue);
                for (Object step : Objects.requireNonNull(listStep)) {
                    for (Constructor c : step.getClass().getDeclaredConstructors()) {
                        Class[] parameterTypes = c.getParameterTypes();
                        if (parameterTypes.length == 2 && parameterTypes[0] == Integer.TYPE && parameterTypes[1] == Integer.class) {
                            return step.getClass();
                        }
                    }
                }
            }

            throw new Exception("Unable to find AudioAlertTone type via Beep Pattern");
        } catch (Exception e) {
            Log.w("KI2", "Unable to get AudioAlertTone via method 2", e);
        }

        return null;
    });

    private static final Lazy<Class<?>> TYPE_AUDIO_ALERT_TONE = LazyKt.lazy(() -> {
        try {
            Class<?> audioAlertToneType = TYPE_AUDIO_ALERT_TONE_1.getValue();

            if (audioAlertToneType != null) {
                return audioAlertToneType;
            }

            audioAlertToneType = TYPE_AUDIO_ALERT_TONE_2.getValue();
            if (audioAlertToneType != null) {
                return audioAlertToneType;
            }

            throw new Exception("Unable to get AudioAlertTone type");
        } catch (Exception e) {
            Log.w("KI2", "Unable to get AudioAlertTone", e);
        }

        return null;
    });

    private static final Lazy<Constructor<?>> CONSTRUCTOR_AUDIO_ALERT_TONE = LazyKt.lazy(() -> {
        try {
            return TYPE_AUDIO_ALERT_TONE.getValue().getDeclaredConstructor(Integer.TYPE, Integer.class);
        } catch (Exception e) {
            Log.w("KI2", "Unable to get AudioAlertTone constructor", e);
        }

        return null;
    });

    private static final Lazy<Class<? extends Enum>> TYPE_AUDIO_ALERT = LazyKt.lazy(() -> {
        try {
            return (Class<? extends Enum>) Class.forName("io.hammerhead.datamodels.profiles.AudioAlert");
        } catch (Exception e) {
            Log.w("KI2", "Unable to get audio alert type", e);
        }

        return null;
    });

    private static final Lazy<Object> BEEP_PATTERN_WORKOUT_INTERVAL = LazyKt.lazy(() -> {
        try {
            Class<? extends Enum> enumType = TYPE_BEEP_PATTERN.getValue();
            return Enum.valueOf(enumType, "WORKOUT_INTERVAL");
        } catch (Exception e) {
            Log.w("KI2", "Unable to get bet pattern WORKOUT_INTERVAL", e);
        }

        return null;
    });

    private static final Lazy<Object> BEEP_PATTERN_AUTO_LAP = LazyKt.lazy(() -> {
        try {
            Class<? extends Enum> enumType = TYPE_BEEP_PATTERN.getValue();
            return Enum.valueOf(enumType, "AUTO_LAP");
        } catch (Exception e) {
            Log.w("KI2", "Unable to get bet pattern AUTO_LAP", e);
        }

        return null;
    });

    private static final Lazy<Object> BEEP_PATTERN_BELL = LazyKt.lazy(() -> {
        try {
            Class<? extends Enum> enumType = TYPE_BEEP_PATTERN.getValue();
            return Enum.valueOf(enumType, "BELL");
        } catch (Exception e) {
            Log.w("KI2", "Unable to get bet pattern BELL", e);
        }

        return null;
    });

    private static final Lazy<Object> BEEP_PATTERN_SINGLE = LazyKt.lazy(() -> {
        try {
            Constructor<?> constructor = TYPE_BEEP_PATTERN.getValue().getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            return constructor.newInstance("SINGLE_BEEP", 0x100, Collections.singletonList(createTone(350, 5000)));
        } catch (Exception e) {
            Log.w("KI2", "Unable to create single beep pattern", e);
        }

        return null;
    });

    private static final Lazy<Object> BEEP_PATTERN_DOUBLE = LazyKt.lazy(() -> {
        try {
            Constructor<?> constructor = TYPE_BEEP_PATTERN.getValue().getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            return constructor.newInstance("DOUBLE_BEEP", 0x100, Arrays.asList(
                    createTone(350, 5000),
                    createRest(100),
                    createTone(350, 5000)));
        } catch (Exception e) {
            Log.w("KI2", "Unable to create double beep pattern", e);
        }

        return null;
    });

    private static final Lazy<Object> AUDIO_ALERT_AUTO_LAP = LazyKt.lazy(() -> {
        try {
            return AudioAlertHook.getAudioAlert("AUTO_LAP");
        } catch (Exception e) {
            Log.w("KI2", "Unable to get AUTO_LAP audio alert", e);
        }

        return null;
    });

    private static final Lazy<Object> AUDIO_ALERT_WORKOUT_NEW_INTERVAL = LazyKt.lazy(() -> {
        try {
            return AudioAlertHook.getAudioAlert("WORKOUT_NEW_INTERVAL");
        } catch (Exception e) {
            Log.w("KI2", "Unable to get WORKOUT_NEW_INTERVAL audio alert", e);
        }

        return null;
    });

    private static final Lazy<Object> AUDIO_ALERT_BELL = LazyKt.lazy(() -> {
        try {
            return AudioAlertHook.getAudioAlert("BELL");
        } catch (Exception e) {
            Log.w("KI2", "Unable to get BELL audio alert", e);
        }

        return null;
    });

    private static ThrowingConsumer<Object> METHOD_BEEP;
    private static ThrowingBiConsumer<Object, String> METHOD_AUDIO_ALERT;

    private static boolean initialized;

    private static void init(SdkContext context) {
        if (initialized) {
            return;
        }

        try {
            Class<?> classActivityServiceApplication = Class.forName("io.hammerhead.activityservice.ActivityServiceApplication");
            Field[] fieldsInActivityServiceApplication = classActivityServiceApplication.getDeclaredFields();

            for (Field fieldActivityComponent : fieldsInActivityServiceApplication) {
                fieldActivityComponent.setAccessible(true);
                Object activityComponent = fieldActivityComponent.get(context.getBaseContext());

                if (activityComponent == null) {
                    continue;
                }

                Method[] methodsInActivityComponent = activityComponent.getClass().getDeclaredMethods();
                for (Method method : methodsInActivityComponent) {
                    Object activityController = method.invoke(activityComponent);

                    if (activityController == null) {
                        continue;
                    }

                    Field[] fieldsInActivityController = activityController.getClass().getDeclaredFields();

                    for (Field fieldListActivityLifecycleListener : fieldsInActivityController) {
                        if (!List.class.isAssignableFrom(fieldListActivityLifecycleListener.getType())) {
                            continue;
                        }

                        fieldListActivityLifecycleListener.setAccessible(true);
                        List<?> listActivityLifecycleListener = (List<?>) fieldListActivityLifecycleListener.get(activityController);

                        if (listActivityLifecycleListener == null || listActivityLifecycleListener.isEmpty()) {
                            continue;
                        }

                        for (Object activityLifecycleListener : listActivityLifecycleListener) {
                            Field[] fieldsInAudioAlertReceiver = activityLifecycleListener.getClass().getDeclaredFields();

                            for (Field fieldListAudioAlertManager : fieldsInAudioAlertReceiver) {
                                if (!List.class.isAssignableFrom(fieldListAudioAlertManager.getType())) {
                                    continue;
                                }

                                fieldListAudioAlertManager.setAccessible(true);
                                List<?> listAudioAlertManager = (List<?>) fieldListAudioAlertManager.get(activityLifecycleListener);

                                if (listAudioAlertManager == null || listAudioAlertManager.isEmpty()) {
                                    continue;
                                }

                                for (Object audioAlertManager : listAudioAlertManager) {
                                    Method[] methodsAudioAlertManager = audioAlertManager.getClass().getDeclaredMethods();
                                    boolean isAudioAlertManager = Arrays.stream(audioAlertManager.getClass().getFields()).anyMatch(f -> f.getType() == TextToSpeech.class);

                                    Method methodBeep = null;
                                    Method methodAudioAlert = null;
                                    for (Method m : methodsAudioAlertManager) {
                                        Class<?>[] types = m.getParameterTypes();

                                        if (types.length == 1 && types[0] == METHOD_BEEP_PATTERN_GET_STEPS.getValue().getReturnType()) {
                                            m.setAccessible(true);
                                            methodBeep = m;
                                        }

                                        if (types.length == 2 && types[0] == TYPE_AUDIO_ALERT.getValue() && types[1] == String.class) {
                                            m.setAccessible(true);
                                            methodAudioAlert = m;
                                        }
                                    }

                                    if (methodBeep != null) {
                                        Method m = methodBeep;
                                        METHOD_BEEP = o -> m.invoke(audioAlertManager, o);
                                    }

                                    if (isAudioAlertManager && methodAudioAlert != null) {
                                        Method m = methodAudioAlert;

                                        if (METHOD_AUDIO_ALERT == null) {
                                            METHOD_AUDIO_ALERT = (o, str) -> m.invoke(audioAlertManager, o, str);
                                        } else {
                                            ThrowingBiConsumer<Object, String> existingMethodAudioAlert = METHOD_AUDIO_ALERT;
                                            METHOD_AUDIO_ALERT = (o, str) -> {
                                                existingMethodAudioAlert.accept(o, str);
                                                m.invoke(audioAlertManager, o, str);
                                            };
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (METHOD_BEEP == null || METHOD_AUDIO_ALERT == null) {
                Log.w("KI2", "Unable to initialize audio methods");
            }
        } catch (Exception e) {
            Log.e("KI2", "Unable to initialize audio methods", e);
        }

        initialized = true;
    }

    @Nullable
    private static Object createTone(int duration, int frequency) {
        try {
            return CONSTRUCTOR_AUDIO_ALERT_TONE.getValue().newInstance(duration, frequency);
        } catch (Exception e) {
            Log.e("KI2", "Unable to create tone AudioAlertTone", e);
        }

        return null;
    }

    @Nullable
    private static Object createRest(int duration) {
        try {
            return CONSTRUCTOR_AUDIO_ALERT_TONE.getValue().newInstance(duration, null);
        } catch (Exception e) {
            Log.e("KI2", "Unable to create rest AudioAlertTone", e);
        }

        return null;
    }

    private static boolean beep(SdkContext sdkContext, Object beepPattern, Object audioAlert) {
        init(sdkContext);

        if (METHOD_BEEP == null || METHOD_AUDIO_ALERT == null) {
            return false;
        }

        try {
            METHOD_BEEP.accept(METHOD_BEEP_PATTERN_GET_STEPS.getValue().invoke(beepPattern));
            METHOD_AUDIO_ALERT.accept(audioAlert, null);
            return true;
        } catch (Exception e) {
            Log.e("KI2", "Unable to beep", e);
        }

        return false;
    }

    private static final Lazy<Class<?>> TYPE_PARCELABLE_LIST = LazyKt.lazy(() -> {
        try {
            return Class.forName("io.hammerhead.aidlrx.ParcelableList");
        } catch (Exception e) {
            Log.w("KI2", "Unable to get ParcelableList type", e);
        }

        return null;
    });

    private static final Lazy<Constructor<?>> CONSTRUCTOR_PARCELABLE_LIST = LazyKt.lazy(() -> {
        try {
            return TYPE_PARCELABLE_LIST.getValue().getDeclaredConstructor(List.class);
        } catch (Exception e) {
            Log.w("KI2", "Unable to get ParcelableList constructor", e);
        }

        return null;
    });

    private static boolean beepCustom(SdkContext context, Object beepPattern) {
        try {
            Intent intent = new Intent("io.hammerhead.action.AUDIO_ALERT");
            intent.setAction("io.hammerhead.action.CUSTOM_AUDIO_ALERT");
            intent.putExtra("tones", (Parcelable) CONSTRUCTOR_PARCELABLE_LIST.getValue().newInstance(METHOD_BEEP_PATTERN_GET_STEPS.getValue().invoke(beepPattern)));

            context.sendBroadcast(intent);
        } catch (Exception e) {
            Log.w("KI2", "Unable to send custom audio alert intent", e);
            return false;
        }

        return true;
    }

    /**
     * Beep karoo 'Auto Lap'.
     *
     * @param context Sdk context.
     * @return True if the alert was triggered, False otherwise.
     */
    public static boolean beepKarooAutoLap(SdkContext context) {
        return beep(context, BEEP_PATTERN_AUTO_LAP.getValue(), AUDIO_ALERT_AUTO_LAP.getValue());
    }

    /**
     * Beep Karoo 'Workout Interval'.
     *
     * @param context Sdk context.
     * @return True if the alert was triggered, False otherwise.
     */
    public static boolean beepKarooWorkoutInterval(SdkContext context) {
        return beep(context, BEEP_PATTERN_WORKOUT_INTERVAL.getValue(), AUDIO_ALERT_WORKOUT_NEW_INTERVAL.getValue());
    }

    /**
     * Beep Karoo 'Bell'.
     *
     * @param context Sdk context.
     * @return True if the alert was triggered, False otherwise.
     */
    public static boolean beepKarooBell(SdkContext context) {
        return beep(context, BEEP_PATTERN_BELL.getValue(), AUDIO_ALERT_BELL.getValue());
    }

    /**
     * Single Beep.
     *
     * @param context Sdk context.
     * @return True if the alert was triggered, False otherwise.
     */
    public static boolean beepSingle(SdkContext context) {
        return beep(context, BEEP_PATTERN_SINGLE.getValue(), AUDIO_ALERT_AUTO_LAP.getValue())
                || beepCustom(context, BEEP_PATTERN_SINGLE.getValue());
    }

    /**
     * Double beep.
     *
     * @param context Sdk context.
     * @return True if the alert was triggered, False otherwise.
     */
    public static boolean beepDouble(SdkContext context) {
        return beep(context, BEEP_PATTERN_DOUBLE.getValue(), AUDIO_ALERT_WORKOUT_NEW_INTERVAL.getValue())
                || beepCustom(context, BEEP_PATTERN_DOUBLE.getValue());
    }

}
