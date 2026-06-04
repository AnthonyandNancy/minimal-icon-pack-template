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

import java.util.Random;

/**
 * Moisture-like liquid glass Drawable — 6 layers + optional noise texture.
 *
 * Layers:
 *   1. Cool-tinted semi-transparent base
 *   2. Top-left → bottom-right soft white highlight gradient
 *   3. Hairline bright top edge
 *   4. Bottom subtle shadow gradient for depth
 *   5. 1.2dp white highlight stroke
 *   6. Micro noise (optional, alpha ≤ 0.03)
 */
public class LiquidGlassDrawable extends Drawable {

    private final Paint fillPaint       = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hlPaint         = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint edgePaint       = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint noisePaint      = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random rng            = new Random(42L);

    private final float radiusPx;
    private final int   baseColor;
    private final int   strokeColor;
    private final float highlightAlpha;
    private final float shadowAlpha;
    private final float noiseAlpha;
    private final float strokeWidthPx;
    private final float[] cornerRadii;
    private final RectF  rect = new RectF();
    private final Path   clip = new Path();

    private boolean noiseDrawn;

    public LiquidGlassDrawable(float density, float radiusDp,
                               int baseColor, int strokeColor,
                               float highlightAlpha, float shadowAlpha,
                               float noiseAlpha, float[] cornerRadii) {
        this.radiusPx       = radiusDp * density;
        this.baseColor      = baseColor;
        this.strokeColor    = strokeColor;
        this.highlightAlpha = highlightAlpha;
        this.shadowAlpha    = shadowAlpha;
        this.noiseAlpha     = noiseAlpha;
        this.strokeWidthPx  = 1.2f * density;
        this.cornerRadii    = cornerRadii;

        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(strokeWidthPx);
        edgePaint.setStyle(Paint.Style.STROKE);
        edgePaint.setStrokeWidth(0.6f * density);
        noisePaint.setStrokeWidth(1f * density * 0.7f);
    }

    // Uniform corners
    public LiquidGlassDrawable(float density, float radiusDp,
                               int baseColor, int strokeColor,
                               float highlightAlpha, float shadowAlpha,
                               float noiseAlpha) {
        this(density, radiusDp, baseColor, strokeColor,
                highlightAlpha, shadowAlpha, noiseAlpha, null);
    }

    @Override
    protected void onBoundsChange(android.graphics.Rect b) {
        super.onBoundsChange(b);
        float half = strokeWidthPx / 2f;
        rect.set(b.left + half, b.top + half, b.right - half, b.bottom - half);
        clip.reset();
        if (cornerRadii != null) clip.addRoundRect(rect, cornerRadii, Path.Direction.CW);
        else clip.addRoundRect(rect, radiusPx, radiusPx, Path.Direction.CW);
    }

    @Override
    public void draw(Canvas canvas) {
        int save = canvas.save();
        canvas.clipPath(clip);

        // 1. Base fill
        fillPaint.setColor(baseColor);
        fillPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(rect, fillPaint);

        // 2. Highlight gradient (top-left → bottom-right)
        if (highlightAlpha > 0) {
            hlPaint.setShader(new LinearGradient(rect.left, rect.top,
                    rect.right, rect.bottom,
                    argb((int)(255 * highlightAlpha), 0xFF, 0xFF, 0xFF),
                    argb((int)(255 * highlightAlpha * 0.1f), 0xFF, 0xFF, 0xFF),
                    Shader.TileMode.CLAMP));
            hlPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(rect, hlPaint);
        }

        // 3. Top bright hairline
        edgePaint.setColor(argb(160, 0xFF, 0xFF, 0xFF));
        float topY = rect.top + strokeWidthPx * 0.8f;
        canvas.drawLine(rect.left + radiusPx * 0.35f, topY,
                rect.right - radiusPx * 0.35f, topY, edgePaint);

        // 4. Bottom subtle shadow
        if (shadowAlpha > 0) {
            shadowPaint.setShader(new LinearGradient(
                    rect.left, rect.bottom - radiusPx * 1.2f,
                    rect.left, rect.bottom,
                    argb(0, 0, 0, 0),
                    argb((int)(255 * shadowAlpha), 0x0A, 0x12, 0x20),
                    Shader.TileMode.CLAMP));
            shadowPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(rect, shadowPaint);
        }

        // 5. Noise texture (single-pass, cached)
        if (noiseAlpha > 0 && !noiseDrawn) {
            int step = (int)(radiusPx * 0.25f);
            if (step < 3) step = 3;
            noisePaint.setColor(argb((int)(255 * noiseAlpha), 0xFF, 0xFF, 0xFF));
            for (float x = rect.left + step; x < rect.right - step; x += step) {
                for (float y = rect.top + step; y < rect.bottom - step; y += step) {
                    if (rng.nextFloat() > 0.5f) {
                        canvas.drawPoint(x + rng.nextFloat() * step,
                                y + rng.nextFloat() * step, noisePaint);
                    }
                }
            }
            noiseDrawn = true;
        } else if (noiseAlpha > 0) {
            // Replay cached noise (same seed = same pattern)
            int step = (int)(radiusPx * 0.25f);
            if (step < 3) step = 3;
            noisePaint.setColor(argb((int)(255 * noiseAlpha), 0xFF, 0xFF, 0xFF));
            Random r = new Random(42L);
            for (float x = rect.left + step; x < rect.right - step; x += step) {
                for (float y = rect.top + step; y < rect.bottom - step; y += step) {
                    if (r.nextFloat() > 0.5f) {
                        canvas.drawPoint(x + r.nextFloat() * step,
                                y + r.nextFloat() * step, noisePaint);
                    }
                }
            }
        }

        canvas.restoreToCount(save);

        // 6. Stroke (outside clip)
        strokePaint.setColor(strokeColor);
        if (cornerRadii != null) {
            Path sp = new Path(); sp.addRoundRect(rect, cornerRadii, Path.Direction.CW);
            canvas.drawPath(sp, strokePaint);
        } else {
            canvas.drawRoundRect(rect, radiusPx, radiusPx, strokePaint);
        }
    }

    @Override public void setAlpha(int a) {}
    @Override public void setColorFilter(ColorFilter f) {}
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }

    private static int argb(int a, int r, int g, int b) {
        return (a << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    // ── A / B / C glass grading system ─────────────────────

    // A-grade: strongest glass — toolbar, hero, drawer, floating bar
    private static LiquidGlassDrawable gradeA(float density, float radius, float alpha) {
        return new LiquidGlassDrawable(density, radius,
                argb((int)(255*alpha), 0xFF,0xFF,0xFF), 0x90FFFFFF, 0.42f, 0.24f, 0.028f);
    }
    public static LiquidGlassDrawable toolbar(float density)     { return gradeA(density, 36, 0.30f); }
    public static LiquidGlassDrawable heroCard(float density)    { return gradeA(density, 32, 0.28f); }
    public static LiquidGlassDrawable floatingBar(float density) { return gradeA(density, 34, 0.32f); }
    public static LiquidGlassDrawable drawerBg(float density) {
        float r = density * 34;
        return new LiquidGlassDrawable(density, 34, 0x38FFFFFF, 0x90FFFFFF, 0.42f, 0.26f, 0.026f,
                new float[]{0, 0, r, r, r, r, 0, 0});
    }

    // B-grade: medium glass — stat/feature/info cards
    private static LiquidGlassDrawable gradeB(float density, float radius, float alpha) {
        return new LiquidGlassDrawable(density, radius,
                argb((int)(255*alpha), 0xFF,0xFF,0xFF), 0x70FFFFFF, 0.34f, 0.20f, 0.020f);
    }
    public static LiquidGlassDrawable statCard(float density)    { return gradeB(density, 28, 0.22f); }
    public static LiquidGlassDrawable featureCard(float density) { return gradeB(density, 26, 0.20f); }
    public static LiquidGlassDrawable aboutCard(float density)   { return gradeB(density, 24, 0.18f); }

    // C-grade: light glass — list items, filter chips, small buttons
    private static LiquidGlassDrawable gradeC(float density, float radius, float alpha) {
        return new LiquidGlassDrawable(density, radius,
                argb((int)(255*alpha), 0xFF,0xFF,0xFF), 0x50FFFFFF, 0.28f, 0.14f, 0.012f);
    }
    public static LiquidGlassDrawable glassButton(float density) { return gradeC(density, 999, 0.18f); }
    public static LiquidGlassDrawable listItem(float density)    { return gradeC(density, 22, 0.14f); }
}
