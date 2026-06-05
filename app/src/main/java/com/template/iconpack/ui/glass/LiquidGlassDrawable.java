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
 * 1:1 replica of rdev/liquid-glass-react rendering pipeline.
 *
 * Key visual traits:
 *   - backdrop-filter: blur()         → blurred background sampling
 *   - glass tint + saturation wash    → semi-transparent white + blue/purple/cyan gradient
 *   - inner glow (radial from center) → radial gradient bright at center
 *   - directional top-left highlight  → linear gradient 135°
 *   - edge displacement               → multi-offset bright + dark strokes
 *   - chromatic aberration            → R/G/B channel edge strokes at different offsets
 *   - top hairline                    → 0.3dp white line at top
 *   - bottom shadow                   → dark gradient from bottom
 *   - noise grain                     → fixed-seed random dots
 *   - screen-blend border rim         → white stroke with SCREEN xfermode
 *   - overlay inner rim               → thinner inner white rim
 *   - solid outer rim                 → configurable stroke color
 */
public class LiquidGlassDrawable extends Drawable {

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final Path clip = new Path();

    private final GlassMaterialConfig cfg;
    private final float d, rPx;
    private View hostView;
    private Bitmap blurredBg;
    private boolean bgCaptured;

    public LiquidGlassDrawable(GlassMaterialConfig config, float density) {
        this.cfg = config; this.d = density;
        this.rPx = config.cornerRadiusDp * d;
    }

    public void attachTo(View view) { this.hostView = view; }

    @Override protected void onBoundsChange(android.graphics.Rect b) {
        super.onBoundsChange(b);
        float h = 1.2f * d / 2f;
        rect.set(b.left + h, b.top + h, b.right - h, b.bottom - h);
        clip.reset();
        if (cfg.cornerRadii != null) clip.addRoundRect(rect, cfg.cornerRadii, Path.Direction.CW);
        else clip.addRoundRect(rect, rPx, rPx, Path.Direction.CW);
        bgCaptured = false;
    }

    @Override public void draw(Canvas canvas) {
        int save = canvas.save();
        canvas.clipPath(clip);

        float alpha = cfg.opacity;
        float hl    = cfg.highlightIntensity;
        float edge  = cfg.edgeIntensity;
        float sh    = cfg.bottomShadowIntensity;
        float displ = cfg.displacementScale * d;

        // ═══════════════════════════════════════════════
        // LAYER 0: backdrop-filter: blur()
        // ═══════════════════════════════════════════════
        if (hostView != null && !bgCaptured) {
            float blurPx = cfg.blurAmount * 36f; // maps ~0.25 → 9dp blur
            blurredBg = BlurUtils.captureAndBlur(hostView, blurPx, d);
            bgCaptured = true;
        }
        if (blurredBg != null) {
            p.reset(); canvas.drawBitmap(blurredBg, rect.left, rect.top, p);
        }

        // ═══════ LAYER 1: base glass tint ═══════
        p.reset(); p.setColor(cfg.baseColor); p.setAlpha((int)(255 * alpha));
        fillRR(canvas);

        // ═══════ LAYER 2: saturation colour wash ═══════
        float satAlpha = cfg.saturation * cfg.innerGlowIntensity * 4f;
        if (satAlpha > 0.002f) {
            p.reset(); p.setAlpha((int)(255 * Math.min(satAlpha, 0.20f)));
            p.setShader(new LinearGradient(rect.left, rect.top, rect.right, rect.bottom,
                    new int[]{0x5EA8FF, 0x8B5CF6, 0x16D6C8},
                    new float[]{0f, 0.5f, 1f}, Shader.TileMode.CLAMP));
            fillRR(canvas);
        }

        // ═══════ LAYER 3: inner radial glow ═══════
        float glow = cfg.innerGlowIntensity * 2.5f;
        float dim  = Math.min(rect.width(), rect.height());
        if (glow > 0) {
            p.reset();
            p.setShader(new RadialGradient(rect.centerX(), rect.centerY(), dim * 0.48f,
                    argb((int)(255*glow*0.55f), 0xFFFFFF),
                    argb(0, 0xFFFFFF), Shader.TileMode.CLAMP));
            fillRR(canvas);
        }

        // ═══════ LAYER 4: directional highlight (top-left → bottom-right) ═══════
        if (hl > 0) {
            p.reset();
            p.setShader(new LinearGradient(rect.left, rect.top, rect.right, rect.bottom,
                    argb((int)(255*hl*0.95f), 0xFFFFFF),
                    argb((int)(255*hl*0.05f), 0xFFFFFF),
                    Shader.TileMode.CLAMP));
            fillRR(canvas);
        }

        // ═══════ LAYER 5: edge displacement (multi-offset strokes) ═══════
        p.reset(); p.setStyle(Paint.Style.STROKE);

        // Outer bright edge (1.2dp)
        p.setStrokeWidth(1.2f * d);
        p.setColor(argb((int)(255*edge*0.82f), 0xFFFFFF));
        strokeRR(canvas, rect);

        // Offset inner dark edge (glass thickness)
        p.setStrokeWidth(0.45f * d);
        p.setColor(argb((int)(255*edge*0.25f), 0x1A2A3A));
        strokeRR(canvas, margin(rect, displ * 0.32f));

        // ═══════ LAYER 6: RGB chromatic aberration ═══════
        if (cfg.enableChromaticAberration && cfg.aberrationIntensity > 0) {
            float ca = cfg.aberrationIntensity;
            float off = displ * 0.12f * ca;

            // R channel (left-up)
            p.setStrokeWidth(0.5f * d);
            p.setColor(argb((int)(255*ca*0.42f), 0xFF5555));
            strokeRR(canvas, margin(rect, -off));

            // G channel (centre, slightly thinner = blur simulation)
            p.setStrokeWidth(0.32f * d);
            p.setColor(argb((int)(255*ca*0.20f), 0x55FF55));
            strokeRR(canvas, rect);

            // B channel (right-down)
            p.setStrokeWidth(0.5f * d);
            p.setColor(argb((int)(255*ca*0.30f), 0x5555FF));
            strokeRR(canvas, margin(rect, off * 0.7f));
        }

        // ═══════ LAYER 7: top bright hairline ═══════
        p.reset(); p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(0.28f * d);
        p.setColor(argb((int)(255*hl*0.65f), 0xFFFFFF));
        float ty = rect.top + 0.6f * d;
        canvas.drawLine(rect.left + rPx * 0.45f, ty, rect.right - rPx * 0.45f, ty, p);

        // ═══════ LAYER 8: bottom shadow ═══════
        if (sh > 0) {
            p.reset(); p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(rect.left, rect.bottom - rPx * 1.6f, rect.left, rect.bottom + 1,
                    argb(0, 0x000000),
                    argb((int)(255*sh*0.72f), 0x0A1628),
                    Shader.TileMode.CLAMP));
            fillRR(canvas);
        }

        // ═══════ LAYER 9: noise grain ═══════
        if (cfg.enableNoise && cfg.noiseIntensity > 0) {
            p.reset(); p.setColor(argb((int)(255*cfg.noiseIntensity), 0xFFFFFF));
            int step = Math.max(2, (int)(rPx * 0.15f));
            Random r = new Random(42L);
            for (float x = rect.left+step; x < rect.right; x += step)
                for (float y = rect.top+step; y < rect.bottom; y += step)
                    if (r.nextFloat() > 0.52f)
                        canvas.drawPoint(x+r.nextFloat()*step, y+r.nextFloat()*step, p);
        }

        canvas.restoreToCount(save);

        // ═══════════════════════════════════════════════
        // LAYER 10: border rims (screen + overlay + solid)
        // ═══════════════════════════════════════════════
        float rr = cfg.cornerRadii != null ? cfg.cornerRadii[0] : rPx;
        Path bp = new Path();
        if (cfg.cornerRadii != null) bp.addRoundRect(rect, cfg.cornerRadii, Path.Direction.CW);
        else bp.addRoundRect(rect, rr, rr, Path.Direction.CW);

        p.reset(); p.setStyle(Paint.Style.STROKE);

        // Screen blend outer glow rim
        p.setStrokeWidth(d * 0.9f);
        p.setColor(argb((int)(255*edge*0.55f), 0xFFFFFF));
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
        canvas.drawPath(bp, p);
        p.setXfermode(null);

        // Inner white rim (overlay equivalent)
        p.setStrokeWidth(d * 0.5f);
        p.setColor(argb((int)(255*edge*0.38f), 0xFFFFFF));
        RectF inner = new RectF(rect.left + d*0.6f, rect.top + d*0.6f,
                rect.right - d*0.6f, rect.bottom - d*0.6f);
        Path ip = new Path();
        ip.addRoundRect(inner, rr, rr, Path.Direction.CW);
        canvas.drawPath(ip, p);

        // Solid final stroke
        p.setStrokeWidth(1.2f * d);
        p.setColor(cfg.strokeColor);
        canvas.drawPath(bp, p);
    }

    // ── helpers ──────────────────────────────────────
    private void fillRR(Canvas c) { p.setStyle(Paint.Style.FILL); c.drawRoundRect(rect, rPx, rPx, p); }
    private void strokeRR(Canvas c, RectF r) {
        float rr = cfg.cornerRadii != null ? cfg.cornerRadii[0] : rPx;
        Path path = new Path(); path.addRoundRect(r, rr, rr, Path.Direction.CW);
        c.drawPath(path, p);
    }
    private static RectF margin(RectF r, float m) {
        return new RectF(r.left+m, r.top+m, r.right-m, r.bottom-m);
    }

    @Override public void setAlpha(int a) {}
    @Override public void setColorFilter(ColorFilter f) {}
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    private static int argb(int a, int color) { return (a<<24)|(color&0x00FFFFFF); }
}
