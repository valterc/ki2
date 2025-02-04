package com.valterc.ki2.karoo.overlay.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.valterc.ki2.data.connection.ConnectionInfo;
import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.preferences.device.DevicePreferencesView;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.karoo.Ki2ExtensionContext;
import com.valterc.ki2.karoo.overlay.OverlayPreferences;

import java.util.function.Consumer;

public interface IOverlayView {

    /**
     * Setup the overlay in ride.
     */
    void setupInRide();

    /**
     * Remove overlay from view hierarchy.
     */
    void remove();

    /**
     * Show overlay.
     */
    void show();

    /**
     * Hide overlay.
     */
    void hide();

    /**
     * Indicates if the overlay is visible.
     *
     * @return True if the overlay is visible, False otherwise.
     */
    boolean isVisible();

    /**
     * Set a listener to be notified when visibility of the overlay changes.
     *
     * @param visibilityListener Visibility listener or null to clear.
     */
    void setVisibilityListener(Consumer<Boolean> visibilityListener);

    /**
     * Set overlay view alpha.
     *
     * @param value Overlay alpha.
     */
    void setAlpha(float value);

    /**
     * Apply all preferences in the overlay.
     *
     * @param extensionContext Ki2 extension context.
     * @param preferences      Overlay preferences.
     */
    default void applyPreferences(@NonNull Ki2ExtensionContext extensionContext, @NonNull OverlayPreferences preferences) {
        setAlpha(preferences.getOpacity());
    }

    /**
     * Update overlay view state.
     *
     * @param connectionInfo    Device connection info.
     * @param devicePreferences Device preferences view.
     * @param batteryInfo       Device battery info.
     * @param shiftingInfo      Device shifting info.
     */
    void updateView(@NonNull ConnectionInfo connectionInfo,
                    @NonNull DevicePreferencesView devicePreferences,
                    @Nullable BatteryInfo batteryInfo,
                    @Nullable ShiftingInfo shiftingInfo);


}
