package com.valterc.ki2.karoo.overlay;

public class OverlayPreferences {

    private final boolean enabled;
    private final String theme;
    private final int duration;
    private final float opacity;
    private final int positionX;
    private final int positionY;

    public OverlayPreferences(boolean enabled, String theme, int duration, float opacity, int positionX, int positionY) {
        this.enabled = enabled;
        this.theme = theme;
        this.duration = duration;
        this.opacity = opacity;
        this.positionX = positionX;
        this.positionY = positionY;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getTheme() {
        return theme;
    }

    public int getDuration() {
        return duration;
    }

    public float getOpacity() {
        return opacity;
    }

    public int getPositionX() {
        return positionX;
    }

    public int getPositionY() {
        return positionY;
    }
}
