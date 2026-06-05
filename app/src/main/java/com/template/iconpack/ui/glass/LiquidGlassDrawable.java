package com.template.iconpack.ui.glass;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.view.View;

import java.util.Random;

/**
 * Liquid glass Drawable — replicates liquid-glass-react's rendering pipeline:
 *   background blur → displacement edge → chromatic aberration →
 *   highlight → shadow → noise → stroke rim.
 */
public class LiquidGlassDrawable extends Drawable {

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final Path  clip = new Path();
    private final Random rng = new Random(42L);

    private final GlassMaterialConfig cfg;
    private final float d, sp, rPx;
    private View hostView;
    private Bitmap blurredBg;
    private boolean bgCaptured;

    public LiquidGlassDrawable(GlassMaterialConfig config, float density) {
        this.cfg = config;
        this.d = density;
        this.sp = 1.2f * d;
        this.rPx = config.cornerRadiusDp * d;
    }

    /** Attach a host View for background blur sampling. */
    public void attachTo(View view) { this.hostView = view; }

    /** Auto-attach when set as background on a View. */
    @Override public void setCallback(android.graphics.drawable.Drawable.Callback cb) {
        super.setCallback(cb);
        if (cb instanceof View && hostView == null) hostView = (View) cb;
    }

    @Override protected void onBoundsChange(android.graphics.Rect b) {
        super.onBoundsChange(b);
        float h = sp / 2f;
        rect.set(b.left + h, b.top + h, b.right - h, b.bottom - h);
        clip.reset();
        if (cfg.cornerRadii != null) clip.addRoundRect(rect, cfg.cornerRadii, Path.Direction.CW);
        else clip.addRoundRect(rect, rPx, rPx, Path.Direction.CW);

        bgCaptured = false; // force re-capture on resize
    }

    @Override public void draw(Canvas canvas) {
        int save = canvas.save();
        canvas.clipPath(clip);

        // ── LAYER 0: Background blur (like backdrop-filter: blur()) ──
        if (hostView != null && !bgCaptured) captureBg();
        if (blurredBg != null) {
            p.setAlpha(255);
            p.setShader(null);
            p.setXfermode(null);
            p.setStyle(Paint.Style.FILL);
            canvas.drawBitmap(blurredBg, rect.left, rect.top, p);
        }

        float w = rect.width(), h = rect.height();
        float alpha = cfg.opacity;
        float hl = cfg.highlightIntensity;
        float edgeA = cfg.edgeIntensity;
        float sh = cfg.bottomShadowIntensity;

        // ── LAYER 1: Glass tint overlay ──
        p.setShader(null);
        p.setColor(cfg.baseColor);
        p.setAlpha((int)(255 * alpha * 0.7f));
        p.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(rect, rPx, rPx, p);

        // ── LAYER 2: Saturation colour bleed (blue→purple→cyan) ──
        float sat = cfg.saturation * cfg.innerGlowIntensity * 3f;
        if (sat > 0.003f) {
            int[] sc = {0x5EA8FF, 0x8B5CF6, 0x16D6C8};
            p.setShader(new LinearGradient(rect.left, rect.top, rect.right, rect.bottom,
                    sc, new float[]{0, 0.5f, 1f}, Shader.TileMode.CLAMP));
            p.setAlpha((int)(255 * Math.min(sat, 0.18f)));
            p.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(rect, rPx, rPx, p);
        }

        // ── LAYER 3: Inner glow (radial) ──
        float glow = cfg.innerGlowIntensity * 2f;
        if (glow > 0) {
            p.setShader(new RadialGradient(rect.centerX(), rect.centerY(),
                    Math.min(w, h) * 0.4f,
                    argb((int)(255*glow*0.45f), 0xFFFFFF),
                    argb(0, 0xFFFFFF), Shader.TileMode.CLAMP));
            p.setAlpha(255);
            p.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(rect, rPx, rPx, p);
        }

        // ── LAYER 4: Highlight (top-left → bottom-right) ──
        if (hl > 0) {
            p.setShader(new LinearGradient(rect.left, rect.top, rect.right, rect.bottom,
                    argb((int)(255*hl*0.95f), 0xFFFFFF),
                    argb(0, 0xFFFFFF), Shader.TileMode.CLAMP));
            p.setAlpha(255);
            p.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(rect, rPx, rPx, p);
        }

        // ── LAYER 5: Edge refraction (multiple offset strokes) ──
        float displ = cfg.displacementScale * d;
        p.setStyle(Paint.Style.STROKE);

        // Outer bright stroke
        p.setStrokeWidth(sp * 1.1f);
        p.setColor(argb((int)(255*edgeA*0.85f), 0xFFFFFF));
        p.setAlpha(255);
        p.setShader(null);
        drawRR(canvas, rect, 0, 0);

        // Inner darker stroke (0.4dp)
        p.setStrokeWidth(0.4f * d);
        p.setColor(argb((int)(255*edgeA*0.25f), 0x000000));
        RectF inner = inset(rect, displ * 0.35f);
        drawRR(canvas, inner, 0, 0);

        // ── LAYER 6: Chromatic aberration (RGB channel separation) ──
        if (cfg.enableChromaticAberration && cfg.aberrationIntensity > 0) {
            float ca = cfg.aberrationIntensity;
            float off = displ * 0.12f * ca;

            // Red channel (left-up)
            p.setStrokeWidth(0.5f * d);
            p.setColor(argb((int)(255*ca*0.40f), 0xFF6B6B));
            drawRR(canvas, inset(rect, -off * 1.2f), -off * 0.6f, -off * 0.6f);

            // Blue channel (right-down)
            p.setColor(argb((int)(255*ca*0.30f), 0x6B6BFF));
            drawRR(canvas, inset(rect, -off * 0.8f), off * 0.6f, off * 0.6f);

            // Green stays centered (slight blur)
            p.setStrokeWidth(0.3f * d);
            p.setColor(argb((int)(255*ca*0.15f), 0x6BFF6B));
            drawRR(canvas, rect, 0, 0);
        }

        // ── LAYER 7: Top-edge hairline ──
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(0.25f * d);
        p.setShader(null);
        p.setColor(argb((int)(255*hl*0.65f), 0xFFFFFF));
        p.setAlpha(255);
        float ty = rect.top + sp * 0.45f;
        canvas.drawLine(rect.left + rPx * 0.4f, ty, rect.right - rPx * 0.4f, ty, p);

        // ── LAYER 8: Bottom shadow ──
        if (sh > 0) {
            p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(rect.left, rect.bottom - rPx * 1.2f,
                    rect.left, rect.bottom + 1,
                    argb(0, 0x000000),
                    argb((int)(255*sh*0.75f), 0x060E1A),
                    Shader.TileMode.CLAMP));
            p.setAlpha(255);
            canvas.drawRoundRect(rect, rPx, rPx, p);
        }

        // ── LAYER 9: Noise grain ──
        if (cfg.enableNoise && cfg.noiseIntensity > 0) {
            p.setStyle(Paint.Style.FILL);
            p.setShader(null);
            p.setColor(argb((int)(255*cfg.noiseIntensity), 0xFFFFFF));
            int step = Math.max(2, (int)(rPx * 0.15f));
            Random r = new Random(42L);
            for (float x = rect.left + step; x < rect.right; x += step) {
                for (float y = rect.top + step; y < rect.bottom; y += step) {
                    if (r.nextFloat() > 0.55f)
                        canvas.drawPoint(x+r.nextFloat()*step, y+r.nextFloat()*step, p);
                }
            }
        }

        canvas.restoreToCount(save);

        // ── LAYER 10: Stroke rim ──
        p.setStyle(Paint.Style.STROKE);
        p.setShader(null);
        p.setStrokeWidth(sp);
        p.setColor(cfg.strokeColor);
        p.setAlpha(255);
        if (cfg.cornerRadii != null) {
            Path spath = new Path();
            spath.addRoundRect(rect, cfg.cornerRadii, Path.Direction.CW);
            canvas.drawPath(spath, p);
        } else {
            canvas.drawRoundRect(rect, rPx, rPx, p);
        }
    }

    private void captureBg() {
        if (hostView == null) return;
        blurredBg = BlurUtils.captureAndBlur(hostView, cfg.blurAmount * 24f, d);
        bgCaptured = true;
    }

    private void drawRR(Canvas c, RectF r, float ox, float oy) {
        Path path = new Path();
        float rr = cfg.cornerRadii != null ? cfg.cornerRadii[0] : rPx;
        path.addRoundRect(new RectF(r.left+ox, r.top+oy, r.right+ox, r.bottom+oy),
                rr, rr, Path.Direction.CW);
        c.drawPath(path, p);
    }

    private RectF inset(RectF r, float i) {
        return new RectF(r.left + i, r.top + i, r.right - i, r.bottom - i);
    }

    @Override public void setAlpha(int a) {}
    @Override public void setColorFilter(ColorFilter f) {}
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    private static int argb(int a, int color) { return (a<<24)|(color&0x00FFFFFF); }
    public GlassMaterialConfig getConfig() { return cfg; }
}
