package com.valterc.ki2.karoo.hooks;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.utils.ActivityUtils;
import com.valterc.ki2.utils.ProcessUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kotlin.Lazy;
import kotlin.LazyKt;

@SuppressWarnings({"unchecked", "rawtypes"})
@SuppressLint("LogNotTimber")
public final class RideActivityHook {

    private RideActivityHook() {
    }

    private static Integer ID_VIEW_PAGER;
    private static Field FIELD_PAGE_LIST;
    private static Parcelable.Creator PROFILE_ELEMENT_CREATOR;
    private static boolean ACTIVITY_RECREATED;

    private static final Lazy<Class<? extends Enum>> ENUM_PAGE_TYPE = LazyKt.lazy(() -> {
        try {
            return (Class<? extends Enum>) Class.forName("io.hammerhead.datamodels.profiles.PageType");
        } catch (Exception e) {
            Log.w("KI2", "Unable to get page type enum", e);
        }

        return null;
    });

    private static final Lazy<Enum<?>> ENUM_PAGE_TYPE_MAP = LazyKt.lazy(() -> {
        try {
            Class<? extends Enum> classPageTypeEnum = ENUM_PAGE_TYPE.getValue();
            return Enum.valueOf(classPageTypeEnum, "MAP");
        } catch (Exception e) {
            Log.w("KI2", "Unable to get page type map enum", e);
        }

        return null;
    });

    private static final Lazy<Field> FIELD_PAGE_PAGE_TYPE = LazyKt.lazy(() -> {
        try {
            Class<?> classPager = Class.forName("io.hammerhead.datamodels.profiles.Page");
            Field[] classPagerFields = classPager.getFields();

            for (Field field : classPagerFields) {
                if (field.getType() == ENUM_PAGE_TYPE.getValue()) {
                    field.setAccessible(true);
                    return field;
                }
            }
            throw new Exception("Page type field not found");
        } catch (Exception e) {
            Log.w("KI2", "Unable to get page type field", e);
        }

        return null;
    });

    private static final Lazy<Field> FIELD_ELEMENTS = LazyKt.lazy(() -> {
        try {
            Class<?> classPage = Class.forName("io.hammerhead.datamodels.profiles.Page");
            Field[] classPagerFields = classPage.getDeclaredFields();

            for (Field field : classPagerFields) {
                if (Collection.class.isAssignableFrom(field.getType()) &&
                        field.getGenericType().toString().contains("io.hammerhead.datamodels.profiles.ProfileElement")) {
                    field.setAccessible(true);
                    return field;
                }
            }

            throw new Exception("Element list field not found");
        } catch (Exception e) {
            Log.w("KI2", "Unable to get element list field", e);
        }

        return null;
    });

    private static final Lazy<Class<?>> TYPE_PROFILE_ELEMENT = LazyKt.lazy(() -> {
        try {
            return Class.forName("io.hammerhead.datamodels.profiles.ProfileElement");
        } catch (Exception e) {
            Log.w("KI2", "Unable to get ProfileElement type", e);
        }

        return null;
    });

    private static final Lazy<Field> FIELD_CREATOR = LazyKt.lazy(() -> {
        try {
            return TYPE_PROFILE_ELEMENT.getValue().getField("CREATOR");
        } catch (Exception e) {
            Log.w("KI2", "Unable to get ProfileElement creator", e);
        }

        return null;
    });

    private static final Lazy<Field> FIELD_DATA_TYPE = LazyKt.lazy(() -> {
        try {
            Field[] profileElementFields = TYPE_PROFILE_ELEMENT.getValue().getDeclaredFields();

            for (Field field : profileElementFields) {
                if (field.getName().equals("dataType")) {
                    field.setAccessible(true);
                    return field;
                }
            }

            throw new Exception("DataType field not found");
        } catch (Exception e) {
            Log.w("KI2", "Unable to get DataType field", e);
        }

        return null;
    });

    private static final Lazy<Boolean> IS_RIDE_ACTIVITY_PROCESS = LazyKt.lazy(() -> {
        String processName = ProcessUtils.getProcessName();
        return processName != null && processName.contains("io.hammerhead.rideapp");
    });

    /**
     * Indicates if the running code is inside the Ride activity process.
     *
     * @return True if the running process is dedicated to the Ride activity, False otherwise.
     */
    public static boolean isRideActivityProcess() {
        return IS_RIDE_ACTIVITY_PROCESS.getValue();
    }

    @Nullable
    private static ViewPager2 getActivityViewPager() {
        if (!RideActivityHook.isRideActivityProcess()) {
            return null;
        }

        Activity activity = ActivityUtils.getRunningActivity();
        if (activity == null) {
            return null;
        }

        if (ID_VIEW_PAGER != null) {
            View viewViewPager = activity.findViewById(ID_VIEW_PAGER);
            if (viewViewPager instanceof ViewPager2) {
                return (ViewPager2) viewViewPager;
            }
        }

        ViewPager2 viewPager;
        View view = activity.findViewById(android.R.id.content);
        if (view instanceof ViewGroup) {
            viewPager = tryFindViewPager((ViewGroup) view);
            if (viewPager != null) {
                ID_VIEW_PAGER = viewPager.getId();
                return viewPager;
            }
        }

        return null;
    }

    private static ViewPager2 tryFindViewPager(ViewGroup viewGroup) {
        Set<ViewGroup> childViewGroups = new HashSet<>();
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View childView = viewGroup.getChildAt(i);
            if (childView instanceof ViewPager2) {
                return (ViewPager2) childView;
            } else if (childView instanceof ViewGroup) {
                childViewGroups.add((ViewGroup) childView);
            }
        }

        for (ViewGroup childView : childViewGroups) {
            ViewPager2 viewPager = tryFindViewPager(childView);
            if (viewPager != null) {
                return viewPager;
            }
        }

        return null;
    }

    public static boolean switchToMapPage() {
        ViewPager2 viewPager = getActivityViewPager();
        if (viewPager == null) {
            Log.d("KI2", "Unable to get view pager");
            return false;
        }

        RecyclerView.Adapter viewPagerAdapter = viewPager.getAdapter();
        if (viewPagerAdapter == null) {
            Log.w("KI2", "View pager adapter is null");
            return false;
        }

        if (FIELD_PAGE_LIST == null) {
            Field[] fieldsPagerAdapter = viewPagerAdapter.getClass().getFields();
            for (Field field : fieldsPagerAdapter) {
                if (Collection.class.isAssignableFrom(field.getType()) &&
                        field.getGenericType().toString().contains("io.hammerhead.datamodels.profiles.Page")) {
                    FIELD_PAGE_LIST = field;
                }
            }
        }

        if (FIELD_PAGE_LIST == null) {
            Log.w("KI2", "Unable to get field with list of pages");
            return false;
        }

        Collection<?> pages;

        try {
            pages = (Collection<?>) FIELD_PAGE_LIST.get(viewPagerAdapter);
        } catch (Exception e) {
            Log.e("KI2", "Unable to get pages: " + e);
            return false;
        }

        if (pages == null) {
            Log.w("KI2", "List of pages is null");
            return false;
        }

        int index = 0;
        for (Object page : pages) {
            try {
                Object pageType = FIELD_PAGE_PAGE_TYPE.getValue().get(page);
                if (pageType == ENUM_PAGE_TYPE_MAP.getValue()) {
                    viewPager.setCurrentItem(index, false);
                    return true;
                }
            } catch (Exception e) {
                Log.w("KI2", "Unable to check page type: " + e);
            }

            index++;
        }

        return false;
    }

    private static Parcelable recreateProfileElement(Parcelable profileElement) {
        Parcel parcel = Parcel.obtain();
        profileElement.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        if (PROFILE_ELEMENT_CREATOR == null) {
            try {
                PROFILE_ELEMENT_CREATOR = (Parcelable.Creator) FIELD_CREATOR.getValue().get(profileElement);
            } catch (Exception e) {
                Log.w("KI2", "Unable to get ProfileElement creator instance", e);
            }

            if (PROFILE_ELEMENT_CREATOR == null) {
                Log.w("KI2", "Unable to get ProfileElement creator instance");
            }
        }

        return (Parcelable) PROFILE_ELEMENT_CREATOR.createFromParcel(parcel);
    }

    @SuppressLint("NotifyDataSetChanged")
    public static boolean tryRefreshSdkElements() {
        ViewPager2 viewPager = getActivityViewPager();
        if (viewPager == null) {
            Log.d("KI2", "Unable to get view pager");
            return false;
        }

        RecyclerView.Adapter viewPagerAdapter = viewPager.getAdapter();
        if (viewPagerAdapter == null) {
            Log.w("KI2", "View pager adapter is null");
            return false;
        }

        if (FIELD_PAGE_LIST == null) {
            Field[] fieldsPagerAdapter = viewPagerAdapter.getClass().getFields();
            for (Field field : fieldsPagerAdapter) {
                if (Collection.class.isAssignableFrom(field.getType()) &&
                        field.getGenericType().toString().contains("io.hammerhead.datamodels.profiles.Page")) {
                    FIELD_PAGE_LIST = field;
                }
            }
        }

        if (FIELD_PAGE_LIST == null) {
            Log.w("KI2", "Unable to get field with list of pages");
            return false;
        }

        Collection<Object> pages;

        try {
            pages = (Collection<Object>) FIELD_PAGE_LIST.get(viewPagerAdapter);
        } catch (Exception e) {
            Log.e("KI2", "Unable to get pages: " + e);
            return false;
        }

        if (pages == null) {
            Log.w("KI2", "List of pages is null");
            return false;
        }

        int changedUnknownElements = 0;
        int unchangedUnknownElements = 0;

        try {
            for (Object page : pages) {
                List<Object> elements = (List<Object>) FIELD_ELEMENTS.getValue().get(page);
                if (elements == null) {
                    Log.w("KI2", "Elements list is null");
                    return false;
                }

                for (int i = 0; i < elements.size(); i++) {
                    Parcelable profileElement = (Parcelable) elements.get(i);

                    Object existingDataType = FIELD_DATA_TYPE.getValue().get(profileElement);
                    if (existingDataType == null) {
                        continue;
                    }

                    String dataTypeString = existingDataType.toString();
                    if (dataTypeString.contains("DataTypeUnknown")) {
                        profileElement = recreateProfileElement(profileElement);
                        Object newDataType = FIELD_DATA_TYPE.getValue().get(profileElement);
                        if (newDataType != null && existingDataType.getClass() != newDataType.getClass()) {
                            changedUnknownElements++;
                            elements.set(i, profileElement);
                        } else {
                            unchangedUnknownElements++;
                        }
                    }
                }
            }

            if (changedUnknownElements > 0) {
                viewPagerAdapter.notifyDataSetChanged();
                viewPager.postInvalidateDelayed(100);
            }
        } catch (Exception e) {
            Log.e("KI2", "Unable to refresh elements: " + e);
        }

        return !pages.isEmpty() && unchangedUnknownElements == 0;
    }

    public static void registerActivityMonitoring(Ki2Context context) {
        if (!(context.getSdkContext().getBaseContext() instanceof Application)) {
            Log.w("KI2", "Unable to register activity monitor, context is not from an application");
            return;
        }

        ((Application) context.getSdkContext().getBaseContext()).registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                if (activity.getLocalClassName().contains("RideActivity")) {
                    context.getHandler().postDelayed(() -> {
                        boolean result = RideActivityHook.tryRefreshSdkElements();
                        if (!result) {
                            context.getHandler().postDelayed(() -> {
                                boolean resultSecondAttempt = RideActivityHook.tryRefreshSdkElements();
                                if (!resultSecondAttempt && !ACTIVITY_RECREATED) {
                                    ACTIVITY_RECREATED = true;
                                    activity.recreate();
                                }
                            }, 150);
                        }
                    }, 150);
                }
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
            }
        });

        Log.i("KI2", "Registered activity monitor");
    }

}
