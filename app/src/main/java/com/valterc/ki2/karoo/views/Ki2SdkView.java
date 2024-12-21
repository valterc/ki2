package com.valterc.ki2.karoo.views;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;

import androidx.annotation.NonNull;

import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.utils.ColorUtils;

import io.hammerhead.sdk.v0.datatype.view.SdkView;

@SuppressLint("LogNotTimber")
public abstract class Ki2SdkView extends SdkView {

    public static final int COLOR_MAX_DISTANCE = 50;

    private final Ki2Context ki2Context;

    public Ki2SdkView(@NonNull Ki2Context ki2Context) {
        super(ki2Context.getSdkContext());
        this.ki2Context = ki2Context;
    }

    private Integer getFirstNonTransparentBackgroundColor(View view) {
        if (view == null) {
            return null;
        }

        Drawable backgroundDrawable = view.getBackground();

        if (backgroundDrawable instanceof ColorDrawable) {
            ColorDrawable colorDrawable = ((ColorDrawable) backgroundDrawable);
            if (colorDrawable.getAlpha() != 0) {
                return colorDrawable.getColor();
            }
        }

        ViewParent viewParent = view.getParent();
        if (viewParent instanceof View) {
            return getFirstNonTransparentBackgroundColor((View) viewParent);
        }

        return null;
    }

    protected KarooTheme getKarooTheme(View karooHierarchyView){
        if (karooHierarchyView == null) {
            return KarooTheme.WHITE;
        }

        Integer backgroundColor = getFirstNonTransparentBackgroundColor(karooHierarchyView);
        if (backgroundColor == null){
            return KarooTheme.WHITE;
        }

        double[] backgroundColorLAB = new double[3];
        double[] testColorLAB = new double[3];
        ColorUtils.colorToLAB(backgroundColor, backgroundColorLAB);
        ColorUtils.colorToLAB(Color.BLACK, testColorLAB);

        if (ColorUtils.distanceEuclidean(backgroundColorLAB, testColorLAB) <= COLOR_MAX_DISTANCE) {
            Log.d("KI2", "Karoo theme: " + KarooTheme.DARK);
            return KarooTheme.DARK;
        }

        ColorUtils.colorToLAB(Color.WHITE, testColorLAB);
        if (ColorUtils.distanceEuclidean(backgroundColorLAB, testColorLAB) <= COLOR_MAX_DISTANCE) {
            Log.d("KI2", "Karoo theme: " + KarooTheme.WHITE);
            return KarooTheme.WHITE;
        }

        Log.d("KI2", "Karoo theme: " + KarooTheme.WHITE);
        return KarooTheme.WHITE;
    }

    public Ki2Context getKi2Context() {
        return ki2Context;
    }

}
