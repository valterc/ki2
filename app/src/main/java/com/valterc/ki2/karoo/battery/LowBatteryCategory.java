package com.valterc.ki2.karoo.battery;

import android.app.Notification;

public enum LowBatteryCategory {

    LOW(Notification.CATEGORY_EVENT),
    CRITICAL(Notification.CATEGORY_ERROR);

    private final String notificationCategory;

    LowBatteryCategory(String notificationCategory) {
        this.notificationCategory = notificationCategory;
    }

    public String getNotificationCategory() {
        return notificationCategory;
    }
}
