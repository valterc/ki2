package com.valterc.ki2.views.battery;

import android.graphics.Canvas;

public interface IBatteryViewRenderer {

    void updateSettings(BatteryView batteryView);

    void render(BatteryView batteryView, Canvas canvas, int internalWidth, int internalHeight);

}
