package com.valterc.ki2.karoo.hooks;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.hammerhead.sdk.v0.SdkContext;
import kotlin.Lazy;
import kotlin.LazyKt;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class DataSyncServiceHook {

    private static final Lazy<Boolean> IN_ACTIVITY_SERVICE =
            LazyKt.lazy(() -> {
                try {
                    Class.forName("io.hammerhead.datasyncservice.v2.DataSyncService");
                    return true;
                } catch (Exception e) {
                    return false;
                }
            });

    public static boolean isInDataSyncService() {
        return IN_ACTIVITY_SERVICE.getValue();
    }

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
                            Object dataTypeShiftingGear = map.get("TYPE_SHIFTING_GEARS_ID");
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

    private static boolean initialized;

    public static void init(SdkContext context) {
        if (initialized) {
            return;
        }

        try {

            Log.w("KI2", "Data Point: " + TYPE_DATA_POINT.getValue());
            Log.w("KI2", "Data Type: " + DATA_TYPE_SHIFTING_GEARS.getValue());

            Log.w("KI2", "Type device info: " + TYPE_DEVICE_INFO.getValue());
            Log.w("KI2", "Device info: " + DEVICE_INFO.getValue());

            Log.w("KI2", "Type connection type: " + TYPE_CONNECTION_TYPE.getValue());
            Log.w("KI2", "Connection type: " + CONNECTION_TYPE.getValue());

            Log.w("KI2", "Type device: " + TYPE_DEVICE.getValue());
            Log.w("KI2", "Device: " + DEVICE.getValue());

            Log.w("KI2", "Type data source: " + TYPE_DATA_SOURCE.getValue());
            Log.w("KI2", "Data source: " + DATA_SOURCE.getValue());

            Log.w("KI2", "Value: " + getIntValue(15));
            Log.w("KI2", "Data Point: " + getDataPoint(1,50, 1, 11));

            Intent intent = new Intent(context, Class.forName("io.hammerhead.datasyncservice.v2.DataSyncService"));
            intent.setAction("databaseOperationsController");

            ServiceConnection serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {

                    try {
                        Log.i("Ki2", "Service connected: " + name);
                        Log.i("Ki2", "Service instance: " + service + " - " + service.getInterfaceDescriptor() + " - " + service.getClass());

                        Object activityDataController = null;
                        Field[] databaseOperationsFields = service.getClass().getDeclaredFields();
                        for (Field field :
                                databaseOperationsFields) {
                            Log.i("Ki2", "Field: " + field + " - " + field.getType());

                            if (Binder.class.isAssignableFrom(field.getType())) {
                                Binder b = (Binder) field.get(service);
                                IInterface localInterface = b.queryLocalInterface("io.hammerhead.datasyncservice.v2.ActivityDataControllerAIDL");
                                Log.i("Ki2", "Local interface: " + localInterface);
                                if (localInterface != null) {
                                    activityDataController = localInterface;
                                    break;
                                }
                            }
                        }

                        if (activityDataController != null) {
                            Method[] declaredMethods = activityDataController.getClass().getDeclaredMethods();
                            for (Method m : declaredMethods) {
                                Class<?> returnType = m.getReturnType();
                                Class<?>[] parameterTypes = m.getParameterTypes();

                                if (returnType.equals(Void.TYPE) && parameterTypes.length == 1 && parameterTypes[0].equals(Function1.class)) {
                                    Class<?> parameterType = parameterTypes[0];
                                    Log.w("KI2", String.valueOf(parameterType));
                                    Log.w("KI2", String.valueOf(parameterType.getTypeParameters()[0]));

                                    m.invoke(activityDataController, new Function1<Object, Unit>() {
                                        @Override
                                        public Unit invoke(Object o) {

                                            Log.w("KI2", "Invoke object: " + o + " - " + o.getClass());

                                            Method[] declaredMethodsFileEncoder = o.getClass().getDeclaredMethods();
                                            for (Method m : declaredMethodsFileEncoder) {
                                                Class<?> returnTypeOnDataPointMethod = m.getReturnType();
                                                Class<?>[] parameterTypesOnDataPointMethod = m.getParameterTypes();

                                                if (returnTypeOnDataPointMethod.equals(Void.TYPE) &&
                                                        parameterTypesOnDataPointMethod.length == 1 &&
                                                        parameterTypesOnDataPointMethod[0].isAssignableFrom(TYPE_DATA_POINT.getValue())) {
                                                    try {
                                                        m.invoke(o, getDataPoint((int)(Math.random() * 10), 34,1, 11));
                                                    } catch (Exception e) {
                                                        throw new RuntimeException(e);
                                                    }
                                                }


                                            }

                                            return Unit.INSTANCE;
                                        }
                                    });
                                }
                            }
                        }

                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {

                }
            };

            boolean result = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            Log.w("Ki2", "Service bind: " + result);

        } catch (Exception e) {
            Log.e("Ki2", "Service exception: " + e);
        }
    }

}
