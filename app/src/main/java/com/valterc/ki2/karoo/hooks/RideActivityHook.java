package com.valterc.ki2.karoo.hooks;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.valterc.ki2.utils.ActivityUtils;
import com.valterc.ki2.utils.ProcessUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
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

    private static final Lazy<Field> FIELD_PAGER_PAGE_TYPE = LazyKt.lazy(() -> {
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
            Log.w("KI2", "Unable to get page type map enum", e);
        }

        return null;
    });

    /**
     * Indicates if the running code is inside the Ride activity process.
     *
     * @return True if the running process is dedicated from the Ride activity, False otherwise.
     */
    public static boolean isRideActivityProcess() {
        return "io.hammerhead.rideapp:io.hammerhead.rideapp.rideActivityProcess".equals(ProcessUtils.getProcessName());
    }

    public static void preload(Context context) {
        Intent intentRideActivity = new Intent(Intent.ACTION_MAIN);
        intentRideActivity.setClassName("io.hammerhead.rideapp", "io.hammerhead.rideapp.views.ride.RideActivity");
        intentRideActivity.putExtra("ki2.preload", true);
        context.startActivity(intentRideActivity);
    }

    public static void tryHandlePreload(Context context) {
        if (RideActivityHook.isRideActivityProcess()) {
            Activity activity = ActivityUtils.getRunningActivity();
            if (activity != null) {
                boolean preload = activity.getIntent().getBooleanExtra("ki2.preload", false);
                Log.d("KI2", "Ride activity preload extra: " + preload);
                if (preload) {
                    Log.d("KI2", "Finish activity and broadcast events");
                    activity.finishAndRemoveTask();
                    context.sendBroadcast(new Intent("io.hammerhead.hx.intent.action.RIDE_STOP"));
                    context.sendBroadcast(new Intent("io.hammerhead.action.RIDE_APP_NOT_RECORDING_EXITED"));
                }
            }
        }
    }

    @Nullable
    private static ViewPager getActivityViewPager() {
        if (!RideActivityHook.isRideActivityProcess()) {
            return null;
        }

        Activity activity = ActivityUtils.getRunningActivity();
        if (activity == null) {
            return null;
        }

        if (ID_VIEW_PAGER != null) {
            View viewViewPager = activity.findViewById(ID_VIEW_PAGER);
            if (viewViewPager instanceof ViewPager) {
                return (ViewPager) viewViewPager;
            }
        }

        ViewPager viewPager;
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

    private static ViewPager tryFindViewPager(ViewGroup viewGroup) {
        Set<ViewGroup> childViewGroups = new HashSet<>();
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View childView = viewGroup.getChildAt(i);
            if (childView instanceof ViewPager) {
                return (ViewPager) childView;
            } else if (childView instanceof ViewGroup) {
                childViewGroups.add((ViewGroup) childView);
            }
        }

        for (ViewGroup childView : childViewGroups) {
            ViewPager viewPager = tryFindViewPager(childView);
            if (viewPager != null) {
                return viewPager;
            }
        }

        return null;
    }

    public static boolean switchToMapPage() {
        ViewPager viewPager = getActivityViewPager();
        if (viewPager == null) {
            Log.d("KI2", "Unable to get view pager");
            return false;
        }

        PagerAdapter viewPagerAdapter = viewPager.getAdapter();
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
                Object pageType = FIELD_PAGER_PAGE_TYPE.getValue().get(page);
                if (pageType == ENUM_PAGE_TYPE_MAP.getValue()) {
                    viewPager.setCurrentItem(index);
                    return true;
                }
            } catch (Exception e) {
                Log.w("KI2", "Unable to check page type: " + e);
            }

            index++;
        }

        return false;
    }

}
