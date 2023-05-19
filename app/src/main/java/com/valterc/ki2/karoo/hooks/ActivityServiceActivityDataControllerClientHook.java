package com.valterc.ki2.karoo.hooks;

import android.annotation.SuppressLint;
import android.util.Log;

import com.valterc.ki2.data.device.DeviceId;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import io.hammerhead.sdk.v0.SdkContext;
import kotlin.Lazy;
import kotlin.LazyKt;

@SuppressWarnings({"unchecked", "rawtypes"})
@SuppressLint("LogNotTimber")
public class ActivityServiceActivityDataControllerClientHook {

    private ActivityServiceActivityDataControllerClientHook() {
    }

    private static final int MAX_ERRORS = 15;

    private static final Lazy<Class<?>> TYPE_DATA_POINT = LazyKt.lazy(() -> {
        try {
            return Class.forName("io.hammerhead.datamodels.timeseriesData.models.DataPoint");
        } catch (Exception e) {
            Log.w("KI2", "Unable to get data point type", e);
        }

        return null;
    });

    private static final Lazy<Class<?>> TYPE_FIELD = LazyKt.lazy(() -> {
        try {
            return Class.forName("io.hammerhead.datamodels.timeseriesData.models.Field");
        } catch (Exception e) {
            Log.w("KI2", "Unable to get field type", e);
        }

        return null;
    });

    private static final Lazy<Constructor<?>> CONSTRUCTOR_FIELD = LazyKt.lazy(() -> {
        try {
            return TYPE_FIELD.getValue().getConstructor(String.class, Integer.TYPE, Integer.TYPE, Boolean.TYPE);
        } catch (Exception e) {
            Log.w("KI2", "Unable to get field constructor", e);
        }

        return null;
    });

    private static final Lazy<Class<? extends Enum>> TYPE_VALIDATORS = LazyKt.lazy(() -> {
        try {
            return (Class<? extends Enum>) Class.forName("io.hammerhead.datamodels.timeseriesData.models.Validators");
        } catch (Exception e) {
            Log.w("KI2", "Unable to get validators type", e);
        }

        return null;
    });

    private static final Lazy<Integer> VALIDATOR_ORDINAL = LazyKt.lazy(() -> {
        try {
            return Enum.valueOf(TYPE_VALIDATORS.getValue(), "INT_POSITIVE_OR_ZERO").ordinal();
        } catch (Exception e) {
            Log.w("KI2", "Unable to get validator", e);
        }

        return 7;
    });

    private static final Lazy<Object> FIELD_SHIFTING_FRONT_GEAR = LazyKt.lazy(() -> {
        try {
            return CONSTRUCTOR_FIELD.getValue().newInstance("FIELD_SHIFTING_FRONT_GEAR_ID", 0, VALIDATOR_ORDINAL.getValue(), false);
        } catch (Exception e) {
            Log.w("KI2", "Unable to get field shifting front gear", e);
        }

        return null;
    });

    private static final Lazy<Object> FIELD_SHIFTING_FRONT_GEAR_TEETH = LazyKt.lazy(() -> {
        try {
            return CONSTRUCTOR_FIELD.getValue().newInstance("FIELD_SHIFTING_FRONT_GEAR_TEETH_ID", 0, VALIDATOR_ORDINAL.getValue(), true);
        } catch (Exception e) {
            Log.w("KI2", "Unable to get field shifting front gear teeth", e);
        }

        return null;
    });

    private static final Lazy<Object> FIELD_SHIFTING_REAR_GEAR = LazyKt.lazy(() -> {
        try {
            return CONSTRUCTOR_FIELD.getValue().newInstance("FIELD_SHIFTING_REAR_GEAR_ID", 0, VALIDATOR_ORDINAL.getValue(), false);
        } catch (Exception e) {
            Log.w("KI2", "Unable to get field shifting rear gear", e);
        }

        return null;
    });

    private static final Lazy<Object> FIELD_SHIFTING_REAR_GEAR_TEETH = LazyKt.lazy(() -> {
        try {
            return CONSTRUCTOR_FIELD.getValue().newInstance("FIELD_SHIFTING_REAR_GEAR_TEETH_ID", 0, VALIDATOR_ORDINAL.getValue(), true);
        } catch (Exception e) {
            Log.w("KI2", "Unable to get field shifting rear gear teeth", e);
        }

        return null;
    });

    private static final Lazy<Class<?>> TYPE_VALUE = LazyKt.lazy(() -> {
        try {
            return Class.forName("io.hammerhead.datamodels.timeseriesData.models.Value");
        } catch (Exception e) {
            Log.w("KI2", "Unable to get value type", e);
        }

        return null;
    });

    private static final Lazy<Constructor<?>> CONSTRUCTOR_VALUE = LazyKt.lazy(() -> {
        try {
            return TYPE_VALUE.getValue().getConstructor(Object.class, Integer.TYPE);
        } catch (Exception e) {
            Log.w("KI2", "Unable to get value constructor", e);
        }

        return null;
    });

    private final static Lazy<Class<?>> TYPE_DATA_TYPE = LazyKt.lazy(() -> {
        try {
            Field[] declaredFieldsDataPoint = TYPE_DATA_POINT.getValue().getDeclaredFields();
            for (Field f : declaredFieldsDataPoint) {
                if (f.getType().isInterface()) {
                    Class<?>[] declaredClasses = f.getType().getDeclaredClasses();
                    for (Class<?> c : declaredClasses) {
                        Field[] declaredFieldsDataPointInnerClass = c.getDeclaredFields();
                        for (Field fieldDataPointInnerClass : declaredFieldsDataPointInnerClass) {
                            if (Modifier.isStatic(fieldDataPointInnerClass.getModifiers()) &&
                                    fieldDataPointInnerClass.getType().isAssignableFrom(ConcurrentHashMap.class) &&
                                    fieldDataPointInnerClass.getGenericType() instanceof ParameterizedType) {
                                ParameterizedType parameterizedType = (ParameterizedType) fieldDataPointInnerClass.getGenericType();
                                if (parameterizedType.getActualTypeArguments().length == 2 &&
                                        parameterizedType.getActualTypeArguments()[0] == String.class) {
                                    return f.getType();
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.w("KI2", "Unable to get data type type", e);
        }

        return null;
    });

    private static final Lazy<Object> DATA_TYPE_SHIFTING_GEARS = LazyKt.lazy(() -> {
        try {
            Class<?>[] declaredClasses = TYPE_DATA_TYPE.getValue().getDeclaredClasses();
            for (Class<?> c : declaredClasses) {
                Field[] declaredFieldsDataPointInnerClass = c.getDeclaredFields();
                for (Field fieldDataPointInnerClass : declaredFieldsDataPointInnerClass) {
                    if (Modifier.isStatic(fieldDataPointInnerClass.getModifiers()) &&
                            fieldDataPointInnerClass.getType().isAssignableFrom(ConcurrentHashMap.class) &&
                            fieldDataPointInnerClass.getGenericType() instanceof ParameterizedType) {
                        ParameterizedType parameterizedType = (ParameterizedType) fieldDataPointInnerClass.getGenericType();
                        if (parameterizedType.getActualTypeArguments().length == 2 &&
                                parameterizedType.getActualTypeArguments()[0] == String.class) {
                            ConcurrentHashMap<String, ?> map = (ConcurrentHashMap<String, ?>) fieldDataPointInnerClass.get(null);
                            return Objects.requireNonNull(map).get("TYPE_SHIFTING_GEARS_ID");
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.w("KI2", "Unable to get data type type", e);
        }

        return null;
    });

    private static final Lazy<Constructor<?>> CONSTRUCTOR_DATA_POINT = LazyKt.lazy(() -> {
        try {
            return TYPE_DATA_POINT.getValue().getConstructor(Long.TYPE, Long.TYPE, TYPE_DATA_TYPE.getValue(), List.class, Map.class, Map.class, Map.class);
        } catch (Exception e) {
            Log.w("KI2", "Unable to get data point constructor", e);
        }

        return null;
    });

    private static int errors;

    private static Object ACTIVITY_DATA_CONTROLLER_CLIENT;
    private static Method METHOD_ON_DATA_POINT;

    private static void init(SdkContext context) throws Exception {
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
                if (method.getParameterTypes().length != 0) {
                    continue;
                }

                Object activityController = method.invoke(activityComponent);
                if (activityController == null) {
                    continue;
                }

                Field[] fieldsInActivityController = activityController.getClass().getDeclaredFields();
                for (Field fieldActivityManagerAdapter : fieldsInActivityController) {
                    fieldActivityManagerAdapter.setAccessible(true);
                    Object activityManagerAdapter = fieldActivityManagerAdapter.get(activityController);

                    if (activityManagerAdapter == null) {
                        continue;
                    }

                    Field[] fieldsInActivityManagerAdapter = activityManagerAdapter.getClass().getDeclaredFields();
                    for (Field fieldActivitySyncManager : fieldsInActivityManagerAdapter) {
                        fieldActivitySyncManager.setAccessible(true);
                        Object activitySyncManager = fieldActivitySyncManager.get(activityManagerAdapter);

                        if (activitySyncManager == null) {
                            continue;
                        }

                        Field[] fieldsInActivitySyncManager = activitySyncManager.getClass().getDeclaredFields();
                        for (Field fieldDataSyncControllerClient : fieldsInActivitySyncManager) {

                            Method[] methodsDataSyncControllerClient = fieldDataSyncControllerClient.getType().getDeclaredMethods();
                            for (Method methodGetActivityDataControllerClient : methodsDataSyncControllerClient) {
                                Method[] methodsActivityDataControllerClient = methodGetActivityDataControllerClient.getReturnType().getDeclaredMethods();

                                for (Method methodOnDataPoint : methodsActivityDataControllerClient) {
                                    Class<?>[] parameterTypes = methodOnDataPoint.getParameterTypes();
                                    if (methodOnDataPoint.getReturnType() == Boolean.TYPE &&
                                            parameterTypes.length == 1 && parameterTypes[0] == TYPE_DATA_POINT.getValue()) {
                                        ACTIVITY_DATA_CONTROLLER_CLIENT = methodGetActivityDataControllerClient.invoke(fieldDataSyncControllerClient.get(activitySyncManager));
                                        METHOD_ON_DATA_POINT = methodOnDataPoint;
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        throw new Exception("Unable to hook into Activity Data Controller Client");
    }

    private static Object getIntValue(int value) throws Exception {
        return CONSTRUCTOR_VALUE.getValue().newInstance(value, 0);
    }

    private static Object getDataPoint(DeviceId deviceId, int gearFrontIndex, int gearFrontTeeth, int gearRearIndex, int gearRearTeeth) throws Exception {
        Map map = new HashMap();
        map.put(FIELD_SHIFTING_FRONT_GEAR.getValue(), getIntValue(gearFrontIndex));
        map.put(FIELD_SHIFTING_FRONT_GEAR_TEETH.getValue(), getIntValue(gearFrontTeeth));
        map.put(FIELD_SHIFTING_REAR_GEAR.getValue(), getIntValue(gearRearIndex));
        map.put(FIELD_SHIFTING_REAR_GEAR_TEETH.getValue(), getIntValue(gearRearTeeth));

        return CONSTRUCTOR_DATA_POINT.getValue().newInstance(3000,
                System.currentTimeMillis(),
                DATA_TYPE_SHIFTING_GEARS.getValue(),
                Collections.singletonList("SOURCE_DI2_" + deviceId.getDeviceNumber()),
                map,
                Collections.emptyMap(),
                Collections.emptyMap());
    }

    /**
     * Pre-initialize hook.
     *
     * @param context SDK Context.
     */
    public static void preInit(SdkContext context) {
        if (ACTIVITY_DATA_CONTROLLER_CLIENT == null) {
            try {
                init(context);
            } catch (Exception e) {
                Log.w("KI2", "Unable to pre-initialize activity data controller client hook", e);
            }
        }
    }

    /**
     * Report gear shift event to be stored in the FIT file.
     *
     * @param context        SDK Context.
     * @param deviceId       Device identifier.
     * @param frontGearIndex Front gear index.
     * @param frontGearTeeth Front gear teeth count.
     * @param rearGearIndex  Rear gear index.
     * @param rearGearTeeth  Rear gear teeth count.
     * @return True when report is expected to work, false otherwise.
     */
    public static boolean reportGearShift(SdkContext context, DeviceId deviceId, int frontGearIndex, int frontGearTeeth, int rearGearIndex, int rearGearTeeth) {
        if (errors >= MAX_ERRORS) {
            return false;
        }

        try {
            if (ACTIVITY_DATA_CONTROLLER_CLIENT == null) {
                init(context);
            }

            Object dataPoint = getDataPoint(deviceId, frontGearIndex, frontGearTeeth, rearGearIndex, rearGearTeeth);
            METHOD_ON_DATA_POINT.invoke(ACTIVITY_DATA_CONTROLLER_CLIENT, dataPoint);
            return true;
        } catch (Exception e) {
            Log.w("KI2", "Unable to report gear shifting", e);
            errors++;

            if (errors >= MAX_ERRORS) {
                Log.e("KI2", "Max error count reached, stopping further attempts", e);
            }
        }

        return false;
    }

}
