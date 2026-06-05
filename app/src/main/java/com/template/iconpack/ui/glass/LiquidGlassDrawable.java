package com.template.iconpack.ui.glass;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

import java.util.Random;

/**
 * Multi-layer liquid glass Drawable.
 *
 * Inspired by liquid-glass-react's layered approach:
 *   background tint → colour saturation → highlight → edge refraction →
 *   chromatic aberration → inner glow → noise → shadow → stroke.
 */
public class LiquidGlassDrawable extends Drawable {

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final Path  clip = new Path();
    private final Random rng = new Random(42L);

    private final GlassMaterialConfig cfg;
    private final float d;   // density
    private final float sp;  // stroke px
    private final float rPx; // corner radius px

    public LiquidGlassDrawable(GlassMaterialConfig config, float density) {
        this.cfg = config;
        this.d = density;
        this.sp = 1.2f * d;
        this.rPx = config.cornerRadiusDp * d;
    }

    @Override protected void onBoundsChange(android.graphics.Rect b) {
        super.onBoundsChange(b);
        float h = sp / 2f;
        rect.set(b.left + h, b.top + h, b.right - h, b.bottom - h);
        clip.reset();
        if (cfg.cornerRadii != null) clip.addRoundRect(rect, cfg.cornerRadii, Path.Direction.CW);
        else clip.addRoundRect(rect, rPx, rPx, Path.Direction.CW);
    }

    @Override public void draw(Canvas canvas) {
        int save = canvas.save();
        canvas.clipPath(clip);

        float w = rect.width(), h = rect.height();
        float alpha = cfg.opacity;

        // ═══════ LAYER 1: Base glass tint ═══════
        p.setShader(null);
        p.setColor(cfg.baseColor);
        p.setAlpha((int)(255 * alpha));
        p.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(rect, rPx, rPx, p);

        // ═══════ LAYER 2: Saturation colour wash (blue→purple→cyan) ═══════
        float sat = cfg.saturation * cfg.innerGlowIntensity * 2.0f;
        if (sat > 0.005f) {
            int[] satColors = {0x5EA8FF, 0x8B5CF6, 0x16D6C8};
            float[] satStops = {0f, 0.5f, 1f};
            p.setShader(new LinearGradient(rect.left, rect.top, rect.right, rect.bottom,
                    satColors, satStops, Shader.TileMode.CLAMP));
            p.setAlpha((int)(255 * Math.min(sat, 0.25f)));
            p.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(rect, rPx, rPx, p);
        }

        // ═══════ LAYER 3: Inner soft glow (radial from centre) ═══════
        float glow = cfg.innerGlowIntensity * 1.8f;
        if (glow > 0) {
            float cx = rect.centerX(), cy = rect.centerY();
            float hr = Math.min(w, h) * 0.38f;
            p.setShader(new RadialGradient(cx, cy, hr,
                    argb((int)(255*glow*0.5f), 0xFFFFFF),
                    argb(0, 0xFFFFFF), Shader.TileMode.CLAMP));
            p.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(rect, rPx, rPx, p);
        }

        // ═══════ LAYER 4: Directional highlight (top-left → bottom-right) ═══════
        float hl = cfg.highlightIntensity;
        if (hl > 0) {
            p.setShader(new LinearGradient(rect.left, rect.top, rect.right, rect.bottom,
                    argb((int)(255*hl*0.95f), 0xFFFFFF),
                    argb((int)(255*hl*0.02f), 0xFFFFFF),
                    Shader.TileMode.CLAMP));
            p.setAlpha(255);
            p.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(rect, rPx, rPx, p);
        }

        // ═══════ LAYER 5: Edge refraction strokes (multiple thin offsets) ═══════
        float edgeA = cfg.edgeIntensity;
        float disp  = cfg.displacementScale * d; // scale to pixels
        p.setStyle(Paint.Style.STROKE);

        // Outer bright edge (1.2dp)
        p.setStrokeWidth(sp);
        p.setColor(argb((int)(255*edgeA*0.9f), 0xFFFFFF));
        p.setAlpha(255);
        drawRoundPath(canvas, rect, 0, 0); // centre

        // Inner darker edge (0.5dp, offset inward)
        p.setStrokeWidth(0.5f * d);
        p.setColor(argb((int)(255*edgeA*0.3f), 0x000000));
        p.setAlpha((int)(255* cfg.bottomShadowIntensity * 0.5f));
        RectF inner = new RectF(rect.left + disp*0.4f, rect.top + disp*0.4f,
                rect.right - disp*0.4f, rect.bottom - disp*0.4f);
        drawRoundPath(canvas, inner, 0, 0);

        // ═══════ LAYER 6: Chromatic aberration (blue/purple edge bleed) ═══════
        if (cfg.enableChromaticAberration && cfg.aberrationIntensity > 0) {
            float ca = cfg.aberrationIntensity;
            p.setStyle(Paint.Style.STROKE);

            // Blue offset (left-up ~0.6dp)
            p.setStrokeWidth(0.6f * d);
            p.setColor(argb((int)(255*ca*0.45f), 0x5EA8FF));
            RectF blue = new RectF(rect.left - disp*0.15f, rect.top - disp*0.15f,
                    rect.right - disp*0.15f, rect.bottom - disp*0.15f);
            drawRoundPath(canvas, blue, 0, 0);

            // Purple offset (right-down ~0.6dp)
            p.setColor(argb((int)(255*ca*0.35f), 0x8B5CF6));
            RectF purple = new RectF(rect.left + disp*0.15f, rect.top + disp*0.15f,
                    rect.right + disp*0.15f, rect.bottom + disp*0.15f);
            drawRoundPath(canvas, purple, 0, 0);
        }

        // ═══════ LAYER 7: Top hairline highlight ═══════
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(0.3f * d);
        p.setColor(argb((int)(255*hl*0.7f), 0xFFFFFF));
        p.setAlpha(255);
        float ty = rect.top + sp * 0.5f;
        canvas.drawLine(rect.left + rPx * 0.35f, ty, rect.right - rPx * 0.35f, ty, p);

        // ═══════ LAYER 8: Bottom shadow (depth) ═══════
        float sh = cfg.bottomShadowIntensity;
        if (sh > 0) {
            p.setStyle(Paint.Style.FILL);
            p.setStrokeWidth(0);
            p.setShader(new LinearGradient(rect.left, rect.bottom - rPx * 1.25f,
                    rect.left, rect.bottom + 1,
                    argb(0, 0x000000),
                    argb((int)(255*sh*0.8f), 0x0A1220),
                    Shader.TileMode.CLAMP));
            p.setAlpha(255);
            canvas.drawRoundRect(rect, rPx, rPx, p);
        }

        // ═══════ LAYER 9: Noise grain ═══════
        if (cfg.enableNoise && cfg.noiseIntensity > 0) {
            int step = Math.max(2, (int)(rPx * 0.18f));
            p.setStyle(Paint.Style.FILL);
            p.setStrokeWidth(0.5f * d);
            p.setShader(null);
            p.setColor(argb((int)(255*cfg.noiseIntensity), 0xFFFFFF));
            Random r = new Random(42L);
            for (float x = rect.left + step; x < rect.right; x += step) {
                for (float y = rect.top + step; y < rect.bottom; y += step) {
                    if (r.nextFloat() > 0.55f) {
                        canvas.drawPoint(x + r.nextFloat()*step, y + r.nextFloat()*step, p);
                    }
                }
            }
        }

        canvas.restoreToCount(save);

        // ═══════ LAYER 10: Outer stroke rim ═══════
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(sp);
        p.setShader(null);
        p.setColor(cfg.strokeColor);
        p.setAlpha(255);
        if (cfg.cornerRadii != null) {
            Path sp = new Path(); sp.addRoundRect(rect, cfg.cornerRadii, Path.Direction.CW);
            canvas.drawPath(sp, p);
        } else {
            canvas.drawRoundRect(rect, rPx, rPx, p);
        }
    }

    private void drawRoundPath(Canvas c, RectF r, float ox, float oy) {
        Path path = new Path();
        float rr = cfg.cornerRadii != null ? cfg.cornerRadii[0] : rPx;
        RectF adj = new RectF(r.left + ox, r.top + oy, r.right + ox, r.bottom + oy);
        path.addRoundRect(adj, rr, rr, Path.Direction.CW);
        c.drawPath(path, p);
    }

    @Override public void setAlpha(int a) {}
    @Override public void setColorFilter(ColorFilter f) {}
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }

    private static int argb(int a, int r, int g, int b) { return (a<<24)|((r&0xFF)<<16)|((g&0xFF)<<8)|(b&0xFF); }
    private static int argb(int a, int color) { return (a<<24)|(color&0x00FFFFFF); }
    public GlassMaterialConfig getConfig() { return cfg; }
}
