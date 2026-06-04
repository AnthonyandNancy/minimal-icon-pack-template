package com.template.iconpack.ui;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.LinearGradient;
import android.graphics.Shader;

import java.util.Arrays;

/**
 * Factory for creating glass-effect Drawables programmatically.
 * No PNG assets needed — everything is drawn via GradientDrawable and LayerDrawable.
 */
public final class GlassDrawableFactory {

    private GlassDrawableFactory() {}

    /**
     * Creates a glass-card background with semi-transparent fill,
     * subtle gradient, rounded corners, and highlight stroke.
     */
    public static Drawable glassCardBg(int radiusDp, float density) {
        return glassCardBg(radiusDp, density, GlassTheme.GLASS_BG_CARD);
    }

    public static Drawable glassCardBg(int radiusDp, float density, int fillColor) {
        int radius = (int) (radiusDp * density);

        // Base fill with subtle top-to-bottom gradient
        GradientDrawable base = new GradientDrawable();
        base.setShape(GradientDrawable.RECTANGLE);
        base.setCornerRadius(radius);
        base.setColors(new int[]{
                GlassTheme.GLASS_STROKE_BRIGHT, // top: brighter
                fillColor                        // bottom: base
        });
        base.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);

        // Stroke layer: thin bright outline
        GradientDrawable stroke = new GradientDrawable();
        stroke.setShape(GradientDrawable.RECTANGLE);
        stroke.setCornerRadius(radius);
        stroke.setStroke((int) (1.5f * density), GlassTheme.GLASS_STROKE_DIM);
        stroke.setColor(android.graphics.Color.TRANSPARENT);

        LayerDrawable layer = new LayerDrawable(new Drawable[]{base, stroke});
        layer.setLayerInset(1, 0, 0, 0, 0);
        return layer;
    }

    /**
     * Glass toolbar background — thicker, more opaque.
     */
    public static Drawable glassToolbarBg(int radiusDp, float density) {
        int radius = (int) (radiusDp * density);

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(radius);
        bg.setColor(GlassTheme.GLASS_BG_TOOLBAR);

        GradientDrawable stroke = new GradientDrawable();
        stroke.setShape(GradientDrawable.RECTANGLE);
        stroke.setCornerRadius(radius);
        stroke.setStroke((int) (1.5f * density), GlassTheme.GLASS_STROKE_BRIGHT);
        stroke.setColor(android.graphics.Color.TRANSPARENT);

        return new LayerDrawable(new Drawable[]{bg, stroke});
    }

    /**
     * Capsule button with glass background.
     */
    public static Drawable glassButtonBg(float density, int fillColor) {
        int radius = (int) (999 * density); // capsule = huge radius

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(radius);
        bg.setColor(fillColor);

        GradientDrawable stroke = new GradientDrawable();
        stroke.setShape(GradientDrawable.RECTANGLE);
        stroke.setCornerRadius(radius);
        stroke.setStroke((int) (1f * density), GlassTheme.GLASS_STROKE_DIM);
        stroke.setColor(android.graphics.Color.TRANSPARENT);

        return new LayerDrawable(new Drawable[]{bg, stroke});
    }

    /**
     * Glass hero card — slight blue-purple gradient overlay.
     */
    public static Drawable glassHeroBg(int radiusDp, float density) {
        int radius = (int) (radiusDp * density);

        GradientDrawable base = new GradientDrawable();
        base.setShape(GradientDrawable.RECTANGLE);
        base.setCornerRadius(radius);
        int[] colors = {
                GlassTheme.GLASS_BG_HERO,
                GlassTheme.GLASS_BG_CARD
        };
        base.setColors(colors);
        base.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        base.setOrientation(GradientDrawable.Orientation.TL_BR);

        GradientDrawable stroke = new GradientDrawable();
        stroke.setShape(GradientDrawable.RECTANGLE);
        stroke.setCornerRadius(radius);
        stroke.setStroke((int) (1.5f * density), GlassTheme.GLASS_STROKE_BRIGHT);
        stroke.setColor(android.graphics.Color.TRANSPARENT);

        return new LayerDrawable(new Drawable[]{base, stroke});
    }

    /**
     * Glass menu item selected background — rounded rectangle.
     */
    public static Drawable glassMenuSelectedBg(int radiusDp, float density) {
        int radius = (int) (radiusDp * density);

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(radius);
        bg.setColor(GlassTheme.GLASS_SELECTED_BG);

        return bg;
    }

    /**
     * Glass search bar background.
     */
    public static Drawable glassSearchBg(float density) {
        int radius = (int) (24 * density);

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(radius);
        bg.setColor(GlassTheme.GLASS_BG_CARD);

        GradientDrawable stroke = new GradientDrawable();
        stroke.setShape(GradientDrawable.RECTANGLE);
        stroke.setCornerRadius(radius);
        stroke.setStroke((int) (1f * density), GlassTheme.GLASS_STROKE_DIM);
        stroke.setColor(android.graphics.Color.TRANSPARENT);

        return new LayerDrawable(new Drawable[]{bg, stroke});
    }

    /**
     * Simple circle background (for icon containers).
     */
    public static Drawable glassCircleBg(int sizeDp, int color, float density) {
        int size = (int) (sizeDp * density);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setSize(size, size);
        bg.setColor(color);
        return bg;
    }

    /**
     * Status badge — small rounded rectangle.
     */
    public static Drawable glassBadge(int color, float density) {
        int radius = (int) (8 * density);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(radius);
        bg.setColor(color);
        return bg;
    }

    /**
     * Progress bar track + fill drawable.
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

    // Helper for hex color parsing
}
