package com.valterc.ki2.karoo.hooks;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.valterc.ki2.data.device.DeviceId;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import kotlin.Lazy;
import kotlin.LazyKt;

@SuppressWarnings({"UnusedReturnValue", "unchecked", "rawtypes"})
@SuppressLint("LogNotTimber")
public class DataSyncServiceHook {

    private static final int MAX_ERRORS = 15;

    private DataSyncServiceHook() {
    }

    private static final Lazy<Class<?>> TYPE_DATA_POINT = LazyKt.lazy(() -> {
        try {
            return Class.forName("io.hammerhead.datamodels.timeseriesData.models.DataPoint");
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

    @SuppressWarnings("unused")
    private static final Lazy<Object> DATA_TYPE_SHIFTING_BATTERY = LazyKt.lazy(() -> {
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
                            return Objects.requireNonNull(map).get("TYPE_SHIFTING_BATTERY_ID");
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.w("KI2", "Unable to get data type type", e);
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

    private static boolean initialized;
    private static int errors;

    private static IBinder binderDatabaseOperations;
    private static IBinder binderActivityController;

    /**
     * Initialize DataSyncService hook.
     *
     * @param context Context.
     */
    public static void init(Context context) {
        if (initialized) {
            return;
        }

        try {
            ServiceConnection serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    binderDatabaseOperations = service;
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    binderDatabaseOperations = null;
                }
            };

            Intent intent = new Intent();
            intent.setComponent(new ComponentName("io.hammerhead.datasyncservice", "io.hammerhead.datasyncservice.v2.DataSyncService"));
            intent.setAction("databaseOperationsController");
            boolean result = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            if (!result) {
                throw new Exception("Unable to bind to service");
            }
        } catch (Exception e) {
            Log.e("KI2", "Unable to initialize DataSyncService hook", e);
        }

        initialized = true;
    }

    private static void trySendDataPoint(Parcelable dataPoint) throws Exception {
        if (binderActivityController != null) {
            sendDataPoint(binderActivityController, dataPoint);
            return;
        }

        int code = 11;
        while (code <= 15) {
            Parcel input = Parcel.obtain();
            Parcel output = Parcel.obtain();
            input.writeInterfaceToken("io.hammerhead.datasyncservice.v2.DatabaseOperationsAIDL");

            try {
                boolean result = binderDatabaseOperations.transact(code, input, output, 0);
                if (result) {
                    try {
                        output.readException();
                        IBinder activityControllerBinder = output.readStrongBinder();
                        if (activityControllerBinder != null) {
                            sendDataPoint(activityControllerBinder, dataPoint);
                            binderActivityController = activityControllerBinder;
                            return;
                        }
                    } catch (Exception e) {
                        Log.w("KI2", "Exception when transacting with service, code: " + code, e);
                    }
                }
            } finally {
                input.recycle();
                output.recycle();
            }

            code++;
        }
        throw new Exception("Unable to find ActivityDataController");
    }


    private static void sendDataPoint(IBinder binderActivityController, Parcelable dataPoint) throws Exception {
        Parcel input = Parcel.obtain();
        Parcel output = Parcel.obtain();

        try {
            Bundle bundle = new Bundle(1);
            bundle.putParcelable("value", dataPoint);

            input.writeInterfaceToken("io.hammerhead.datasyncservice.v2.ActivityDataControllerAIDL");
            input.writeInt(1);
            input.writeBundle(bundle);
            boolean result = binderActivityController.transact(4, input, output, 0);

            if (!result) {
                throw new Exception("Unable to communicate with activity data controller");
            }

            output.readException();
        } finally {
            input.recycle();
            output.recycle();
        }
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
     * Report gear shift event to be stored in the FIT file.
     *
     * @param deviceId       Device identifier.
     * @param frontGearIndex Front gear index.
     * @param frontGearTeeth Front gear teeth count.
     * @param rearGearIndex  Rear gear index.
     * @param rearGearTeeth  Rear gear teeth count.
     * @return True when report is expected to work, false otherwise.
     */
    public static boolean reportGearShift(DeviceId deviceId, int frontGearIndex, int frontGearTeeth, int rearGearIndex, int rearGearTeeth) {
        if (!initialized || errors >= MAX_ERRORS) {
            return false;
        }

        try {
            Parcelable dataPoint = (Parcelable) getDataPoint(deviceId, frontGearIndex, frontGearTeeth, rearGearIndex, rearGearTeeth);
            trySendDataPoint(dataPoint);
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
