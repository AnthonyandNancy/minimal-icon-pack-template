package com.template.iconpack.ui.glass;

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
 * Multi-layer liquid glass Drawable driven by GlassMaterialConfig.
 * 7–8 layers: base → colour glow → highlight → chromatic edge → shadow → noise → stroke.
 */
public class LiquidGlassDrawable extends Drawable {

    private final Paint fillPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hlPaint     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint edgePaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint caPaint     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint noisePaint  = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final GlassMaterialConfig cfg;
    private final float density;
    private final float strokePx;
    private final RectF rect = new RectF();
    private final Path  clip = new Path();
    private final Random rng  = new Random(42L);

    public LiquidGlassDrawable(GlassMaterialConfig config, float density) {
        this.cfg = config;
        this.density = density;
        this.strokePx = 1.2f * density;
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(strokePx);
        edgePaint.setStyle(Paint.Style.STROKE);
        edgePaint.setStrokeWidth(0.5f * density);
        caPaint.setStyle(Paint.Style.STROKE);
        caPaint.setStrokeWidth(0.8f * density);
        noisePaint.setStrokeWidth(0.6f * density);
    }

    @Override
    protected void onBoundsChange(android.graphics.Rect b) {
        super.onBoundsChange(b);
        float h = strokePx / 2f;
        rect.set(b.left + h, b.top + h, b.right - h, b.bottom - h);
        clip.reset();
        float r = cfg.cornerRadiusDp * density;
        if (cfg.cornerRadii != null) clip.addRoundRect(rect, cfg.cornerRadii, Path.Direction.CW);
        else clip.addRoundRect(rect, r, r, Path.Direction.CW);
    }

    @Override
    public void draw(Canvas canvas) {
        int save = canvas.save();
        canvas.clipPath(clip);

        float alpha = cfg.opacity;
        float rPx = cfg.cornerRadiusDp * density;

        // 1. Base
        fillPaint.setColor(argb((int)(255*alpha), cfg.baseColor));
        fillPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(rect, fillPaint);

        // 2. Colour glow (saturation blend)
        float glowA = cfg.innerGlowIntensity * cfg.saturation * 1.5f;
        if (glowA > 0) {
            int[] glows = {0x885EA8FF, 0x447C4DFF, 0x2216D6C8};
            glowPaint.setShader(new LinearGradient(rect.left, rect.top,
                    rect.right, rect.bottom, glows, new float[]{0f, 0.45f, 1f},
                    Shader.TileMode.CLAMP));
            glowPaint.setAlpha((int)(255 * glowA));
            glowPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(rect, glowPaint);
        }

        // 3. Highlight (top-left → bottom-right)
        float hlA = cfg.highlightIntensity;
        if (hlA > 0) {
            hlPaint.setShader(new LinearGradient(rect.left, rect.top,
                    rect.right, rect.bottom,
                    argb((int)(255*hlA), 0xFFFFFF),
                    argb((int)(255*hlA*0.06f), 0xFFFFFF),
                    Shader.TileMode.CLAMP));
            hlPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(rect, hlPaint);
        }

        // 4. Top bright hairline
        float ey = rect.top + strokePx * 0.5f;
        float edgeA = cfg.edgeIntensity;
        edgePaint.setColor(argb((int)(255*edgeA*0.9f), 0xFFFFFF));
        canvas.drawLine(rect.left + rPx*0.3f, ey, rect.right - rPx*0.3f, ey, edgePaint);

        // 5. Chromatic aberration edges
        if (cfg.enableChromaticAberration && cfg.aberrationIntensity > 0) {
            float ca = cfg.aberrationIntensity;
            caPaint.setColor(argb((int)(255*ca*0.5f), 0x5EA8FF)); // blue left
            Path caPath = new Path(); caPath.addRoundRect(rect, rPx, rPx, Path.Direction.CW);
            canvas.drawPath(caPath, caPaint);
            caPaint.setColor(argb((int)(255*ca*0.35f), 0x7C4DFF)); // purple right
            RectF caRect = new RectF(rect.left + density*0.5f, rect.top + density*0.5f,
                    rect.right + density*0.5f, rect.bottom + density*0.5f);
            Path caPath2 = new Path(); caPath2.addRoundRect(caRect, rPx, rPx, Path.Direction.CW);
            canvas.drawPath(caPath2, caPaint);
        }

        // 6. Bottom shadow
        float shA = cfg.bottomShadowIntensity;
        if (shA > 0) {
            shadowPaint.setShader(new LinearGradient(
                    rect.left, rect.bottom - rPx*1.3f, rect.left, rect.bottom,
                    argb(0, 0x000000),
                    argb((int)(255*shA), 0x0A1220),
                    Shader.TileMode.CLAMP));
            shadowPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(rect, shadowPaint);
        }

        // 7. Noise
        if (cfg.enableNoise && cfg.noiseIntensity > 0) {
            int step = Math.max(3, (int)(rPx * 0.22f));
            noisePaint.setColor(argb((int)(255*cfg.noiseIntensity), 0xFFFFFF));
            Random r = new Random(42L);
            for (float x = rect.left + step; x < rect.right - step; x += step) {
                for (float y = rect.top + step; y < rect.bottom - step; y += step) {
                    if (r.nextFloat() > 0.5f)
                        canvas.drawPoint(x + r.nextFloat()*step, y + r.nextFloat()*step, noisePaint);
                }
            }
        }

        canvas.restoreToCount(save);

        // 8. Stroke (outside clip)
        strokePaint.setColor(cfg.strokeColor);
        if (cfg.cornerRadii != null) {
            Path sp = new Path(); sp.addRoundRect(rect, cfg.cornerRadii, Path.Direction.CW);
            canvas.drawPath(sp, strokePaint);
        } else {
            canvas.drawRoundRect(rect, rPx, rPx, strokePaint);
        }
    }

    @Override public void setAlpha(int a) {}
    @Override public void setColorFilter(ColorFilter f) {}
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }

    private static int argb(int a, int color) {
        return (a << 24) | (color & 0x00FFFFFF);
    }
    public GlassMaterialConfig getConfig() { return cfg; }
}
