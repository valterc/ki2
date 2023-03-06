package com.valterc.ki2.karoo.hooks;

import android.annotation.SuppressLint;
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

    private static final Lazy<Class<?>> TYPE_BEEP_PATTERN_STEP_ONE_SHOT = LazyKt.lazy(() -> {
        try {
            Class<? extends Enum> type = TYPE_BEEP_PATTERN.getValue();
            Enum[] enumConstants = type.getEnumConstants();
            for (Enum enumValue : Objects.requireNonNull(enumConstants)) {
                List<?> listStep = (List<?>) METHOD_BEEP_PATTERN_GET_STEPS.getValue().invoke(enumValue);
                for (Object step : Objects.requireNonNull(listStep)) {
                    for (Constructor c : step.getClass().getDeclaredConstructors()) {
                        Class[] parameterTypes = c.getParameterTypes();
                        if (parameterTypes.length == 2 && parameterTypes[0] == Integer.TYPE && parameterTypes[1] == Long.TYPE) {
                            return step.getClass();
                        }
                    }
                }
            }

            throw new Exception("Unable to find OneShot type");
        } catch (Exception e) {
            Log.w("KI2", "Unable to get OneShot type", e);
        }

        return null;
    });

    private static final Lazy<Class<?>> TYPE_BEEP_PATTERN_STEP_REST = LazyKt.lazy(() -> {
        try {
            Class<? extends Enum> type = TYPE_BEEP_PATTERN.getValue();
            Enum[] enumConstants = type.getEnumConstants();
            for (Enum enumValue : Objects.requireNonNull(enumConstants)) {
                List<?> listStep = (List<?>) METHOD_BEEP_PATTERN_GET_STEPS.getValue().invoke(enumValue);
                for (Object step : Objects.requireNonNull(listStep)) {
                    for (Constructor c : step.getClass().getDeclaredConstructors()) {
                        Class[] parameterTypes = c.getParameterTypes();
                        if (parameterTypes.length == 1 && parameterTypes[0] == Long.TYPE) {
                            return step.getClass();
                        }
                    }
                }
            }

            throw new Exception("Unable to find OneShot type");
        } catch (Exception e) {
            Log.w("KI2", "Unable to get OneShot type", e);
        }

        return null;
    });

    private static final Lazy<Constructor<?>> CONSTRUCTOR_BEEP_PATTERN_STEP_ONE_SHOT = LazyKt.lazy(() -> {
        try {
            return TYPE_BEEP_PATTERN_STEP_ONE_SHOT.getValue().getDeclaredConstructor(Integer.TYPE, Long.TYPE);
        } catch (Exception e) {
            Log.w("KI2", "Unable to get OneShot type", e);
        }

        return null;
    });

    private static final Lazy<Constructor<?>> CONSTRUCTOR_BEEP_PATTERN_STEP_REST = LazyKt.lazy(() -> {
        try {
            return TYPE_BEEP_PATTERN_STEP_REST.getValue().getDeclaredConstructor(Long.TYPE);
        } catch (Exception e) {
            Log.w("KI2", "Unable to get OneShot type", e);
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
            Log.w("KI2", "Unable to create single beep pattern", e);
        }

        return null;
    });

    private static final Lazy<Object> BEEP_PATTERN_AUTO_LAP = LazyKt.lazy(() -> {
        try {
            Class<? extends Enum> enumType = TYPE_BEEP_PATTERN.getValue();
            return Enum.valueOf(enumType, "AUTO_LAP");
        } catch (Exception e) {
            Log.w("KI2", "Unable to create single beep pattern", e);
        }

        return null;
    });

    private static final Lazy<Object> BEEP_PATTERN_SINGLE = LazyKt.lazy(() -> {
        try {
            Constructor<?> constructor = TYPE_BEEP_PATTERN.getValue().getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            return constructor.newInstance("SINGLE_BEEP", 0x100, Collections.singletonList(createStepOneShot(5000, 350)));
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
                    createStepOneShot(5000, 350),
                    createStepRest(100),
                    createStepOneShot(5000, 350)));
        } catch (Exception e) {
            Log.w("KI2", "Unable to create double beep pattern", e);
        }

        return null;
    });

    private static final Lazy<Object> AUDIO_ALERT_AUTO_LAP = LazyKt.lazy(() -> {
        try {
            return AudioAlertHook.getAudioAlert("AUTO_LAP");
        } catch (Exception e) {
            Log.w("KI2", "Unable to create audio alert", e);
        }

        return null;
    });

    private static final Lazy<Object> AUDIO_ALERT_WORKOUT_NEW_INTERVAL = LazyKt.lazy(() -> {
        try {
            return AudioAlertHook.getAudioAlert("WORKOUT_NEW_INTERVAL");
        } catch (Exception e) {
            Log.w("KI2", "Unable create double beep pattern", e);
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

                    if (fieldsInActivityController.length == 0) {
                        continue;
                    }

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

                            if (fieldsInAudioAlertReceiver.length == 0) {
                                continue;
                            }

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

                                    Method methodBeep = null;
                                    Method methodAudioAlert = null;
                                    for (Method m : methodsAudioAlertManager) {
                                        Class<?>[] types = m.getParameterTypes();
                                        if (types.length == 1 && types[0] == TYPE_BEEP_PATTERN.getValue()) {
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

                                    if (methodBeep == null && methodAudioAlert != null) {
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
    private static Object createStepOneShot(int frequency, long duration) {
        try {
            return CONSTRUCTOR_BEEP_PATTERN_STEP_ONE_SHOT.getValue().newInstance(frequency, duration);
        } catch (Exception e) {
            Log.e("KI2", "Unable to create BeepPattern Step OneShot instance", e);
        }

        return null;
    }

    @Nullable
    private static Object createStepRest(long duration) {
        try {
            return CONSTRUCTOR_BEEP_PATTERN_STEP_REST.getValue().newInstance(duration);
        } catch (Exception e) {
            Log.e("KI2", "Unable to create BeepPattern Step Rest instance", e);
        }

        return null;
    }

    private static boolean beep(SdkContext sdkContext, Object beepPattern, Object audioAlert) {
        init(sdkContext);

        try {
            METHOD_BEEP.accept(beepPattern);
            METHOD_AUDIO_ALERT.accept(audioAlert, null);
            return true;
        } catch (Exception e) {
            Log.e("KI2", "Unable to beep", e);
        }

        return false;
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
     * Single Beep.
     *
     * @param context Sdk context.
     * @return True if the alert was triggered, False otherwise.
     */
    public static boolean beepSingle(SdkContext context) {
        return beep(context, BEEP_PATTERN_SINGLE.getValue(), AUDIO_ALERT_AUTO_LAP.getValue());
    }

    /**
     * Double beep.
     *
     * @param context Sdk context.
     * @return True if the alert was triggered, False otherwise.
     */
    public static boolean beepDouble(SdkContext context) {
        return beep(context, BEEP_PATTERN_DOUBLE.getValue(), AUDIO_ALERT_WORKOUT_NEW_INTERVAL.getValue());
    }

}
