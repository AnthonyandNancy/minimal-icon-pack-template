package com.template.iconpack.ui;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;

/**
 * Factory for creating glass-effect Drawables programmatically.
 * All backgrounds are semi-transparent — no solid white.
 */
public final class GlassDrawableFactory {

    private GlassDrawableFactory() {}

    /**
     * Standard glass card: semi-transparent fill + thin stroke.
     */
    public static Drawable glassCard(int radiusDp, float density) {
        return glassCard(radiusDp, density, GlassTheme.GLASS_CARD_PRIMARY);
    }

    public static Drawable glassCard(int radiusDp, float density, int fillColor) {
        int radius = (int) (radiusDp * density);

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(radius);
        bg.setColor(fillColor);

        GradientDrawable stroke = new GradientDrawable();
        stroke.setShape(GradientDrawable.RECTANGLE);
        stroke.setCornerRadius(radius);
        stroke.setStroke((int) (0.5f * density), GlassTheme.GLASS_STROKE_DIM);
        stroke.setColor(android.graphics.Color.TRANSPARENT);

        return new LayerDrawable(new Drawable[]{bg, stroke});
    }

    /**
     * Glass toolbar: thicker background with bright stroke.
     */
    public static Drawable glassToolbar(int radiusDp, float density) {
        int radius = (int) (radiusDp * density);

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(radius);
        bg.setColor(GlassTheme.GLASS_TOOLBAR);

        GradientDrawable stroke = new GradientDrawable();
        stroke.setShape(GradientDrawable.RECTANGLE);
        stroke.setCornerRadius(radius);
        stroke.setStroke((int) (0.5f * density), GlassTheme.GLASS_STROKE_BRIGHT);
        stroke.setColor(android.graphics.Color.TRANSPARENT);

        return new LayerDrawable(new Drawable[]{bg, stroke});
    }

    /**
     * Glass drawer background.
     */
    public static Drawable glassDrawer(float density) {
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setColor(GlassTheme.GLASS_DRAWER_BG);
        return bg;
    }

    /**
     * Capsule button with semi-transparent fill.
     */
    public static Drawable glassButton(float density) {
        int radius = (int) (999 * density);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(radius);
        bg.setColor(0xAFFFFFFF); // 68% white

        GradientDrawable stroke = new GradientDrawable();
        stroke.setShape(GradientDrawable.RECTANGLE);
        stroke.setCornerRadius(radius);
        stroke.setStroke((int) (0.5f * density), GlassTheme.GLASS_STROKE_DIM);
        stroke.setColor(android.graphics.Color.TRANSPARENT);

        return new LayerDrawable(new Drawable[]{bg, stroke});
    }

    /**
     * Accent pill button.
     */
    public static Drawable accentPill(float density) {
        int radius = (int) (19 * density);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(radius);
        int[] colors = {GlassTheme.ACCENT_BLUE, GlassTheme.ACCENT_PURPLE};
        bg.setColors(colors);
        bg.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        bg.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);

        return bg;
    }

    /**
     * Search bar background.
     */
    public static Drawable glassSearchBg(float density) {
        int radius = (int) (24 * density);
        return glassCard(radius, density, 0xAFFFFFFF);
    }

    /**
     * Menu item selected background.
     */
    public static Drawable glassMenuSelectedBg(float density) {
        int radius = (int) (18 * density);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(radius);
        bg.setColor(GlassTheme.GLASS_SELECTED_BG);
        return bg;
    }

    /**
     * Icon container circle.
     */
    public static Drawable iconCircleBg(int sizeDp, int color, float density) {
        int size = (int) (sizeDp * density);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setSize(size, size);
        bg.setColor(color);
        return bg;
    }

    /**
     * Progress bar track + fill.
     */
    public static Drawable progressTrack(float density) {
        int radius = (int) (4 * density);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(radius);
        bg.setColor(android.graphics.Color.parseColor("#E5E7EB"));
        return bg;
    }

    public static Drawable progressFill(float density) {
        int radius = (int) (4 * density);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(radius);
        int[] colors = {GlassTheme.ACCENT_BLUE, GlassTheme.ACCENT_PURPLE};
        bg.setColors(colors);
        bg.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        bg.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        return bg;
    }
}
