package com.valterc.ki2.karoo.hooks;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.util.Log;

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
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

@SuppressWarnings({"UnusedReturnValue", "unchecked", "rawtypes"})
@SuppressLint("LogNotTimber")
public class DataSyncServiceHook {

    private DataSyncServiceHook() {
    }

    private static final Lazy<Boolean> IN_ACTIVITY_SERVICE =
            LazyKt.lazy(() -> {
                try {
                    Class.forName("io.hammerhead.datasyncservice.v2.DataSyncService");
                    return true;
                } catch (Exception e) {
                    return false;
                }
            });

    private static final Lazy<Class<?>> TYPE_DATA_POINT = LazyKt.lazy(() -> {
        try {
            return (Class<?>) Class.forName("io.hammerhead.datamodels.timeseriesData.models.DataPoint");
        } catch (Exception e) {
            Log.w("KI2", "Unable to get data point type", e);
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
                            Object dataTypeShiftingGear = Objects.requireNonNull(map).get("TYPE_SHIFTING_GEARS_ID");
                            Log.w("KI2", "Got data type:" + dataTypeShiftingGear);
                            return dataTypeShiftingGear;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.w("KI2", "Unable to get data point type", e);
        }

        return null;
    });

    private static final Lazy<Class<?>> TYPE_DEVICE_INFO = LazyKt.lazy(() -> {
        try {
            return (Class<?>) Class.forName("io.hammerhead.datamodels.timeseriesData.models.DeviceInfo");
        } catch (Exception e) {
            Log.w("KI2", "Unable to get device info type", e);
        }

        return null;
    });

    private static final Lazy<Object> DEVICE_INFO = LazyKt.lazy(() -> {
        try {
            return TYPE_DEVICE_INFO.getValue().getConstructor(String.class, Integer.TYPE).newInstance("Di2", 6);
        } catch (Exception e) {
            Log.w("KI2", "Unable to get device info", e);
        }

        return null;
    });

    private static final Lazy<Class<? extends Enum>> TYPE_CONNECTION_TYPE = LazyKt.lazy(() -> {
        try {
            return (Class<? extends Enum>) Class.forName("io.hammerhead.datamodels.timeseriesData.models.ConnectionType");
        } catch (Exception e) {
            Log.w("KI2", "Unable to get connection type", e);
        }

        return null;
    });

    private static final Lazy<Enum> CONNECTION_TYPE = LazyKt.lazy(() -> {
        try {
            return Enum.valueOf(TYPE_CONNECTION_TYPE.getValue(), "ANT_PLUS");
        } catch (Exception e) {
            Log.w("KI2", "Unable to get connection type value", e);
        }

        return null;
    });

    private static final Lazy<Class<?>> TYPE_DEVICE = LazyKt.lazy(() -> {
        try {
            return Class.forName("io.hammerhead.datamodels.timeseriesData.models.Device");
        } catch (Exception e) {
            Log.w("KI2", "Unable to get device type", e);
        }

        return null;
    });

    private static final Lazy<Object> DEVICE = LazyKt.lazy(() -> {
        try {
            return TYPE_DEVICE.getValue().getConstructor(String.class, TYPE_DEVICE_INFO.getValue(), List.class, TYPE_CONNECTION_TYPE.getValue())
                    .newInstance("DEVICE_DI2", DEVICE_INFO.getValue(), Collections.singletonList(DATA_TYPE_SHIFTING_GEARS), CONNECTION_TYPE.getValue());
        } catch (Exception e) {
            Log.w("KI2", "Unable to get device", e);
        }

        return null;
    });

    private static final Lazy<Class<?>> TYPE_DATA_SOURCE = LazyKt.lazy(() -> {
        try {
            return Class.forName("io.hammerhead.datamodels.timeseriesData.models.DataSource");
        } catch (Exception e) {
            Log.w("KI2", "Unable to get data source type", e);
        }

        return null;
    });

    private static final Lazy<Object> DATA_SOURCE = LazyKt.lazy(() -> {
        try {
            return TYPE_DATA_SOURCE.getValue().getConstructor(String.class, String.class, TYPE_DATA_TYPE.getValue(), TYPE_DEVICE.getValue())
                    .newInstance("SOURCE_DI2", "Di2", DATA_TYPE_SHIFTING_GEARS.getValue(), DEVICE.getValue());
        } catch (Exception e) {
            Log.w("KI2", "Unable to get data source", e);
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

    private static final Lazy<Constructor<?>> CONSTRUCTOR_DATA_POINT = LazyKt.lazy(() -> {
        try {
            return TYPE_DATA_POINT.getValue().getConstructor(Long.TYPE, Long.TYPE, TYPE_DATA_TYPE.getValue(), List.class, Map.class, Map.class, Map.class);
        } catch (Exception e) {
            Log.w("KI2", "Unable to get data point constructor", e);
        }

        return null;
    });

    private static final int MAX_ERRORS = 30;

    private static boolean initialized;
    private static int errors;

    private static Object ACTIVITY_DATA_CONTROLLER;
    private static Class<?> TYPE_FIT_ENCODER;

    /**
     * Initialize DataSyncService hook.
     * @param context SDK Context.
     */
    public static void init(SdkContext context) {
        if (!isInDataSyncService()) {
            return;
        }

        if (initialized) {
            return;
        }

        try {
            ServiceConnection serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    try {
                        Field[] databaseOperationsFields = service.getClass().getDeclaredFields();
                        for (Field field : databaseOperationsFields) {
                            if (Binder.class.isAssignableFrom(field.getType())) {
                                Binder b = (Binder) field.get(service);
                                if (b != null) {
                                    IInterface localInterface = b.queryLocalInterface("io.hammerhead.datasyncservice.v2.ActivityDataControllerAIDL");
                                    if (localInterface != null) {
                                        ACTIVITY_DATA_CONTROLLER = localInterface;
                                        return;
                                    }
                                }
                            }
                        }
                        Log.w("KI2", "Unable to find ActivityDataController");
                    } catch (Exception e) {
                        Log.w("KI2", "Unable to obtain ActivityDataController", e);
                    } finally {
                        context.unbindService(this);
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                }
            };

            Intent intent = new Intent(context, Class.forName("io.hammerhead.datasyncservice.v2.DataSyncService"));
            intent.setAction("databaseOperationsController");
            boolean result = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            Log.w("Ki2", "DataSyncService bind: " + result);
        } catch (Exception e) {
            Log.e("Ki2", "Unable to initialize DataSyncService hook", e);
        }

        initialized = true;
    }

    private static final Lazy<Method> METHOD_ACTIVITY_CONTROLLER_FORWARD_DATA_POINT = LazyKt.lazy(() -> {
        Method[] declaredMethods = ACTIVITY_DATA_CONTROLLER.getClass().getDeclaredMethods();
        for (Method m : declaredMethods) {
            Class<?> returnType = m.getReturnType();
            Class<?>[] parameterTypes = m.getParameterTypes();

            if (returnType.equals(Void.TYPE) && parameterTypes.length == 1 && parameterTypes[0].equals(Function1.class)) {
                return m;
            }
        }
        return null;
    });

    private static final Lazy<Method> METHOD_FIT_ENCODER_ON_DATA_POINT = LazyKt.lazy(() -> {
        Method[] declaredMethodsFileEncoder = TYPE_FIT_ENCODER.getDeclaredMethods();
        for (Method m : declaredMethodsFileEncoder) {
            Class<?> returnTypeOnDataPointMethod = m.getReturnType();
            Class<?>[] parameterTypesOnDataPointMethod = m.getParameterTypes();

            if (returnTypeOnDataPointMethod.equals(Void.TYPE) &&
                    parameterTypesOnDataPointMethod.length == 1 &&
                    parameterTypesOnDataPointMethod[0].isAssignableFrom(TYPE_DATA_POINT.getValue())) {
                return m;
            }
        }
        return null;
    });

    private static Object getIntValue(int value) throws Exception {
        return CONSTRUCTOR_VALUE.getValue().newInstance(value, 0);
    }

    private static Object getDataPoint(int gearFrontIndex, int gearFrontTeeth, int gearRearIndex, int gearRearTeeth) throws Exception {
        Map map = new HashMap();
        map.put(FIELD_SHIFTING_FRONT_GEAR.getValue(), getIntValue(gearFrontIndex));
        map.put(FIELD_SHIFTING_FRONT_GEAR_TEETH.getValue(), getIntValue(gearFrontTeeth));
        map.put(FIELD_SHIFTING_REAR_GEAR.getValue(), getIntValue(gearRearIndex));
        map.put(FIELD_SHIFTING_REAR_GEAR_TEETH.getValue(), getIntValue(gearRearTeeth));

        return CONSTRUCTOR_DATA_POINT.getValue().newInstance(3000,
                System.currentTimeMillis(),
                DATA_TYPE_SHIFTING_GEARS.getValue(),
                Collections.singletonList(DATA_SOURCE.getValue()),
                map,
                Collections.emptyMap(),
                Collections.emptyMap());
    }

    /**
     * Indicates if the running code is inside the Data Sync service application.
     *
     * @return True if the running process is the Data Sync service application, False otherwise.
     */
    public static boolean isInDataSyncService() {
        return IN_ACTIVITY_SERVICE.getValue();
    }

    /**
     * Report gear shift event to be stored in the FIT file.
     *
     * @param frontGearIndex Front gear index.
     * @param frontGearTeeth Front gear teeth count.
     * @param rearGearIndex Rear gear index.
     * @param rearGearTeeth Rear gear teeth count.
     * @return True when report is expected to work, false otherwise.
     */
    public static boolean reportGearShift(int frontGearIndex, int frontGearTeeth, int rearGearIndex, int rearGearTeeth) {
        if (!initialized || ACTIVITY_DATA_CONTROLLER == null || errors >= MAX_ERRORS) {
            return false;
        }

        try {
            METHOD_ACTIVITY_CONTROLLER_FORWARD_DATA_POINT.getValue().invoke(ACTIVITY_DATA_CONTROLLER, (Function1<Object, Unit>) o -> {
                if (TYPE_FIT_ENCODER == null) {
                    TYPE_FIT_ENCODER = o.getClass();
                }

                try {
                    METHOD_FIT_ENCODER_ON_DATA_POINT.getValue().invoke(o, getDataPoint(frontGearIndex, frontGearTeeth, rearGearIndex, rearGearTeeth));
                } catch (Exception e) {
                    Log.w("KI2", "Unable to push data point", e);
                    errors++;
                }

                return Unit.INSTANCE;
            });
        } catch (Exception e) {
            Log.w("KI2", "Unable to report gear shifting", e);
            errors++;
        }

        return true;
    }

}
