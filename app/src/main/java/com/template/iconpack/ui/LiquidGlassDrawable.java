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
 * HarmonyOS 6 high-transparency liquid glass.
 * 7 layers: base → color tint → highlight → top-edge → shadow → noise → stroke.
 */
public class LiquidGlassDrawable extends Drawable {

    private final Paint fillPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tintPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hlPaint     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint edgePaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint noisePaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random rng        = new Random(42L);

    private final float  radiusPx, strokeWidthPx;
    private final int    baseColor, strokeColor;
    private final float  highlightAlpha, shadowAlpha, innerGlow;
    private final float  noiseAlpha;
    private final int    tintColor;       // subtle cool tint for glass color mixing
    private final float  tintAlpha;
    private final float[] cornerRadii;
    private final RectF  rect = new RectF();
    private final Path   clip = new Path();
    private boolean noiseDrawn;

    public LiquidGlassDrawable(float density, float radiusDp,
                               int baseColor, int strokeColor,
                               float highlightAlpha, float shadowAlpha,
                               float innerGlow, float noiseAlpha,
                               int tintColor, float tintAlpha,
                               float[] cornerRadii) {
        this.radiusPx      = radiusDp * density;
        this.baseColor     = baseColor;
        this.strokeColor   = strokeColor;
        this.highlightAlpha = highlightAlpha;
        this.shadowAlpha   = shadowAlpha;
        this.innerGlow     = innerGlow;
        this.noiseAlpha    = noiseAlpha;
        this.tintColor     = tintColor;
        this.tintAlpha     = tintAlpha;
        this.strokeWidthPx = 1.2f * density;
        this.cornerRadii   = cornerRadii;

        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(strokeWidthPx);
        edgePaint.setStyle(Paint.Style.STROKE);
        edgePaint.setStrokeWidth(0.5f * density);
        noisePaint.setStrokeWidth(0.6f * density);
    }

    public LiquidGlassDrawable(float dpDens, float radiusDp,
                               int base, int stroke, float hl, float sh, float ig, float ns,
                               int tint, float ta) {
        this(dpDens, radiusDp, base, stroke, hl, sh, ig, ns, tint, ta, null);
    }

    @Override
    protected void onBoundsChange(android.graphics.Rect b) {
        super.onBoundsChange(b);
        float h = strokeWidthPx / 2f;
        rect.set(b.left + h, b.top + h, b.right - h, b.bottom - h);
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

        // 2. Color tint (simulates glass refracting cool tones) — very subtle
        if (tintAlpha > 0) {
            tintPaint.setShader(new LinearGradient(rect.left, rect.top,
                    rect.right, rect.bottom,
                    argb((int)(255 * tintAlpha), tintColor),
                    argb(0, tintColor), Shader.TileMode.CLAMP));
            tintPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(rect, tintPaint);
        }

        // 3. Inner soft glow (centre-bright)
        if (innerGlow > 0) {
            float cx = rect.centerX(), cy = rect.centerY();
            float rx = rect.width() * 0.40f, ry = rect.height() * 0.35f;
            tintPaint.setShader(new LinearGradient(cx - rx, cy - ry, cx + rx, cy + ry,
                    argb((int)(255 * innerGlow), 0xFF, 0xFF, 0xFF),
                    argb(0, 0xFF, 0xFF, 0xFF), Shader.TileMode.CLAMP));
            tintPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(rect, tintPaint);
        }

        // 4. Highlight (top-left → bottom-right)
        if (highlightAlpha > 0) {
            hlPaint.setShader(new LinearGradient(rect.left, rect.top,
                    rect.right, rect.bottom,
                    argb((int)(255 * highlightAlpha), 0xFF, 0xFF, 0xFF),
                    argb((int)(255 * highlightAlpha * 0.08f), 0xFF, 0xFF, 0xFF),
                    Shader.TileMode.CLAMP));
            hlPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(rect, hlPaint);
        }

        // 5. Bright top-edge hairline
        float ty = rect.top + strokeWidthPx * 0.6f;
        edgePaint.setColor(argb(180, 0xFF, 0xFF, 0xFF));
        canvas.drawLine(rect.left + radiusPx * 0.3f, ty,
                rect.right - radiusPx * 0.3f, ty, edgePaint);

        // 6. Bottom shadow
        if (shadowAlpha > 0) {
            shadowPaint.setShader(new LinearGradient(
                    rect.left, rect.bottom - radiusPx * 1.3f,
                    rect.left, rect.bottom,
                    argb(0, 0, 0, 0),
                    argb((int)(255 * shadowAlpha), 0x06, 0x0E, 0x1A),
                    Shader.TileMode.CLAMP));
            shadowPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(rect, shadowPaint);
        }

        // 7. Noise (cached, same seed)
        if (noiseAlpha > 0) {
            int step = Math.max(3, (int)(radiusPx * 0.22f));
            noisePaint.setColor(argb((int)(255 * noiseAlpha), 0xFF, 0xFF, 0xFF));
            Random r = new Random(42L);
            for (float x = rect.left + step; x < rect.right - step; x += step) {
                for (float y = rect.top + step; y < rect.bottom - step; y += step) {
                    if (r.nextFloat() > 0.5f)
                        canvas.drawPoint(x + r.nextFloat() * step, y + r.nextFloat() * step, noisePaint);
                }
            }
        }

        canvas.restoreToCount(save);

        // 8. Stroke (outside clip)
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
    private static int argb(int a, int color) {
        return (a << 24) | (color & 0x00FFFFFF);
    }

    // ── Light-mode A / B / C grading ──────────────────────

    private static final int TINT_BLUE   = 0xFF5EA8FF;
    private static final int TINT_PURPLE = 0xFF7C4DFF;
    private static final int TINT_CYAN   = 0xFF16D6C8;

    // A-grade: toolbar, hero, drawer, floating-bar (72-88% white)
    private static LiquidGlassDrawable gradeA(float d, float r, float a) {
        return new LiquidGlassDrawable(d, r,
                argb((int)(255*a),0xFF,0xFF,0xFF), 0xD9FFFFFF,
                0.45f, 0.12f, 0.18f, 0.022f, TINT_CYAN, 0.08f);
    }
    public static LiquidGlassDrawable toolbar(float d)     { return gradeA(d, 38, 0.75f); }
    public static LiquidGlassDrawable heroCard(float d)    { return gradeA(d, 34, 0.72f); }
    public static LiquidGlassDrawable floatingBar(float d) { return gradeA(d, 36, 0.80f); }
    public static LiquidGlassDrawable drawerBg(float d) {
        float r = d * 36;
        return new LiquidGlassDrawable(d, 36, 0x72FFFFFF, 0xD9FFFFFF, 0.45f, 0.12f, 0.18f, 0.022f,
                TINT_PURPLE, 0.06f, new float[]{0, 0, r, r, r, r, 0, 0});
    }

    // B-grade: stat/feature/info cards (55-70% white)
    private static LiquidGlassDrawable gradeB(float d, float r, float a) {
        return new LiquidGlassDrawable(d, r,
                argb((int)(255*a),0xFF,0xFF,0xFF), 0xBFFFFFFF,
                0.36f, 0.10f, 0.14f, 0.016f, TINT_BLUE, 0.05f);
    }
    public static LiquidGlassDrawable statCard(float d)    { return gradeB(d, 30, 0.60f); }
    public static LiquidGlassDrawable featureCard(float d) { return gradeB(d, 28, 0.55f); }
    public static LiquidGlassDrawable aboutCard(float d)   { return gradeB(d, 26, 0.50f); }

    // C-grade: list items, chips, small buttons (40-58% white)
    private static LiquidGlassDrawable gradeC(float d, float r, float a) {
        return new LiquidGlassDrawable(d, r,
                argb((int)(255*a),0xFF,0xFF,0xFF), 0x99FFFFFF,
                0.26f, 0.08f, 0.10f, 0.012f, TINT_BLUE, 0.03f);
    }
    public static LiquidGlassDrawable glassButton(float d) { return gradeC(d, 999, 0.50f); }
    public static LiquidGlassDrawable listItem(float d)    { return gradeC(d, 22, 0.40f); }
}
