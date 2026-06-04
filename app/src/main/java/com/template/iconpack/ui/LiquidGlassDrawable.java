package com.template.iconpack.ui;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

/**
 * Multi-layer liquid-glass Drawable — no XML, no MaterialCardView, no white rectangles.
 *
 * Layers (drawn bottom-up):
 *   1. Semi-transparent rounded-rect fill
 *   2. Top-left → bottom-right highlight gradient
 *   3. Top edge bright hairline
 *   4. Bottom subtle shadow gradient
 *   5. 1dp white highlight stroke on all sides
 */
public class LiquidGlassDrawable extends Drawable {

    private final Paint bgPaint       = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint topLinePaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint    = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final float radiusPx;
    private final int   baseColor;
    private final int   strokeColor;
    private final float highlightAlpha;
    private final float shadowAlpha;
    private final float strokeWidthPx;
    private final float[] cornerRadii; // [tl, tl, tr, tr, br, br, bl, bl]

    private final RectF rect = new RectF();
    private final Path  clip = new Path();

    /**
     * @param density         DisplayMetrics.density
     * @param radiusDp        corner radius in dp
     * @param baseColor       semi-transparent fill (e.g. 0x78FFFFFF)
     * @param strokeColor     edge stroke (e.g. 0xB0FFFFFF)
     * @param highlightAlpha  top-left highlight strength (0..1)
     * @param shadowAlpha     bottom shadow strength (0..1)
     * @param cornerRadii     null → all corners = radiusDp; else 8-float array
     */
    public LiquidGlassDrawable(float density, float radiusDp,
                               int baseColor, int strokeColor,
                               float highlightAlpha, float shadowAlpha,
                               float[] cornerRadii) {
        this.radiusPx      = radiusDp * density;
        this.baseColor     = baseColor;
        this.strokeColor   = strokeColor;
        this.highlightAlpha = highlightAlpha;
        this.shadowAlpha   = shadowAlpha;
        this.strokeWidthPx = 1f * density;
        this.cornerRadii   = cornerRadii;

        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(strokeWidthPx);
        topLinePaint.setStyle(Paint.Style.STROKE);
        topLinePaint.setStrokeWidth(0.5f * density);
    }

    // Convenience: uniform corners
    public LiquidGlassDrawable(float density, float radiusDp,
                               int baseColor, int strokeColor,
                               float highlightAlpha, float shadowAlpha) {
        this(density, radiusDp, baseColor, strokeColor, highlightAlpha, shadowAlpha, null);
    }

    @Override
    protected void onBoundsChange(android.graphics.Rect bounds) {
        super.onBoundsChange(bounds);
        rect.set(bounds.left + strokeWidthPx / 2f,
                bounds.top + strokeWidthPx / 2f,
                bounds.right - strokeWidthPx / 2f,
                bounds.bottom - strokeWidthPx / 2f);
        updateClip();
    }

    private void updateClip() {
        clip.reset();
        if (cornerRadii != null) {
            clip.addRoundRect(rect, cornerRadii, Path.Direction.CW);
            strokePaint.setShader(null);
        } else {
            clip.addRoundRect(rect, radiusPx, radiusPx, Path.Direction.CW);
            strokePaint.setShader(null);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        // Save & clip
        int save = canvas.save();
        canvas.clipPath(clip);

        // 1. Base fill
        bgPaint.setColor(baseColor);
        bgPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(rect, bgPaint);

        // 2. Highlight gradient (top-left → bottom-right)
        if (highlightAlpha > 0) {
            highlightPaint.setShader(new LinearGradient(
                    rect.left, rect.top,
                    rect.right, rect.bottom,
                    argb((int) (255 * highlightAlpha), 0xFF, 0xFF, 0xFF),
                    argb(0, 0xFF, 0xFF, 0xFF),
                    Shader.TileMode.CLAMP));
            highlightPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(rect, highlightPaint);
        }

        // 3. Top bright hairline
        topLinePaint.setColor(argb(140, 0xFF, 0xFF, 0xFF));
        canvas.drawLine(rect.left + radiusPx * 0.3f, rect.top + strokeWidthPx,
                rect.right - radiusPx * 0.3f, rect.top + strokeWidthPx, topLinePaint);

        // 4. Bottom subtle shadow
        if (shadowAlpha > 0) {
            shadowPaint.setShader(new LinearGradient(
                    rect.left, rect.bottom - radiusPx,
                    rect.left, rect.bottom,
                    argb(0, 0, 0, 0),
                    argb((int) (255 * shadowAlpha), 0, 0, 0),
                    Shader.TileMode.CLAMP));
            shadowPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(rect.left, rect.bottom - radiusPx,
                    rect.right, rect.bottom, shadowPaint);
        }

        canvas.restoreToCount(save);

        // 5. Stroke (drawn outside clip to avoid clipping)
        strokePaint.setColor(strokeColor);
        if (cornerRadii != null) {
            Path strokePath = new Path();
            strokePath.addRoundRect(rect, cornerRadii, Path.Direction.CW);
            canvas.drawPath(strokePath, strokePaint);
        } else {
            canvas.drawRoundRect(rect, radiusPx, radiusPx, strokePaint);
        }
    }

    @Override public void setAlpha(int alpha) {}
    @Override public void setColorFilter(ColorFilter filter) {}
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }

    private static int argb(int a, int r, int g, int b) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    // ── Factory methods for common components ─────────────

    public static LiquidGlassDrawable toolbar(float density) {
        return new LiquidGlassDrawable(density, 34,
                0xA8FFFFFF, 0xB8FFFFFF, 0.28f, 0.08f);
    }

    public static LiquidGlassDrawable heroCard(float density) {
        return new LiquidGlassDrawable(density, 30,
                0x78FFFFFF, 0xB0FFFFFF, 0.34f, 0.12f);
    }

    public static LiquidGlassDrawable statCard(float density) {
        return new LiquidGlassDrawable(density, 28,
                0x62FFFFFF, 0x9AFFFFFF, 0.38f, 0.14f);
    }

    public static LiquidGlassDrawable featureCard(float density) {
        return new LiquidGlassDrawable(density, 26,
                0x66FFFFFF, 0x96FFFFFF, 0.34f, 0.12f);
    }

    public static LiquidGlassDrawable drawerBg(float density) {
        float r = density * 34;
        return new LiquidGlassDrawable(density, 34,
                0xB5F6FAFF, 0xAFFFFFFF, 0.32f, 0.10f,
                new float[]{0, 0, r, r, r, r, 0, 0});
    }

    public static LiquidGlassDrawable button(float density) {
        return new LiquidGlassDrawable(density, 999,
                0x85FFFFFF, 0x9AFFFFFF, 0.30f, 0.06f);
    }

    public static LiquidGlassDrawable aboutCard(float density) {
        return new LiquidGlassDrawable(density, 24,
                0x66FFFFFF, 0x90FFFFFF, 0.30f, 0.10f);
    }

    public static LiquidGlassDrawable floatingBar(float density) {
        return new LiquidGlassDrawable(density, 30,
                0xA0FFFFFF, 0xB0FFFFFF, 0.30f, 0.10f);
    }
}
