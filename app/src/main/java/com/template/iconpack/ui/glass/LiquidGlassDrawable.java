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
 * Exact replica of rdev/liquid-glass-react (npm: liquid-glass-react).
 *
 * React pipeline → Android equivalent:
 *   backdrop-filter blur()    → BlurUtils real bg sampling + box blur
 *   SVG feDisplacementMap     → multi-offset edge strokes (edge-only)
 *   RGB channel separation    → R/G/B coloured edge strokes at offset positions
 *   screen/overlay borders    → PorterDuff blend mode rim gradients
 *   saturation colour wash    → LinearGradient blue→purple→cyan overlay
 *   radial highlight          → RadialGradient center glow
 *   noise grain               → fixed-seed point noise
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

        // ═══ Layer 0: backdrop-filter blur (real background sampling) ═══
        if (hostView != null && !bgCaptured) {
            blurredBg = BlurUtils.captureAndBlur(hostView, cfg.blurAmount * 24f, d);
            bgCaptured = true;
        }
        if (blurredBg != null) {
            p.reset(); p.setAlpha(255);
            canvas.drawBitmap(blurredBg, rect.left, rect.top, p);
        }

        float alpha = cfg.opacity;
        float hl = cfg.highlightIntensity;
        float edgeA = cfg.edgeIntensity;

        // ═══ Layer 1: Glass tint base ═══
        p.reset(); p.setColor(cfg.baseColor);
        p.setAlpha((int)(255 * alpha));
        canvas.drawRoundRect(rect, rPx, rPx, p);

        // ═══ Layer 2: Saturation colour bleed ═══
        float satAlpha = cfg.saturation * cfg.innerGlowIntensity * 3f;
        if (satAlpha > 0.002f) {
            p.reset(); p.setAlpha((int)(255 * Math.min(satAlpha, 0.16f)));
            p.setShader(new LinearGradient(rect.left, rect.top, rect.right, rect.bottom,
                    new int[]{0x4A9EFF, 0x7B5CF6, 0x16D6C8},
                    new float[]{0f, 0.5f, 1f}, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(rect, rPx, rPx, p);
        }

        // ═══ Layer 3: Radial inner glow (center bright) ═══
        float glow = cfg.innerGlowIntensity * 2.2f;
        if (glow > 0) {
            float dim = Math.min(rect.width(), rect.height());
            p.reset(); p.setAlpha(255);
            p.setShader(new RadialGradient(rect.centerX(), rect.centerY(), dim * 0.45f,
                    argb((int)(255 * glow * 0.42f), 0xFFFFFF),
                    argb(0, 0xFFFFFF), Shader.TileMode.CLAMP));
            canvas.drawRoundRect(rect, rPx, rPx, p);
        }

        // ═══ Layer 4: Directional highlight ═══
        if (hl > 0) {
            p.reset(); p.setAlpha(255);
            p.setShader(new LinearGradient(rect.left, rect.top, rect.right, rect.bottom,
                    argb((int)(255 * hl * 0.95f), 0xFFFFFF),
                    argb((int)(255 * hl * 0.03f), 0xFFFFFF),
                    Shader.TileMode.CLAMP));
            canvas.drawRoundRect(rect, rPx, rPx, p);
        }

        // ═══ Layer 5: Edge displacement (multi-offset, centre stays sharp) ═══
        float displ = cfg.displacementScale * d;
        p.reset(); p.setStyle(Paint.Style.STROKE);

        // Outer bright edge
        p.setStrokeWidth(1.2f * d);
        p.setColor(argb((int)(255 * edgeA * 0.78f), 0xFFFFFF));
        drawRR(canvas, rect);

        // Inner darker edge (glass thickness)
        p.setStrokeWidth(0.4f * d);
        p.setColor(argb((int)(255 * edgeA * 0.22f), 0x152C3E));
        drawRR(canvas, margin(rect, displ * 0.28f));

        // ═══ Layer 6: RGB chromatic aberration at edges ═══
        if (cfg.enableChromaticAberration && cfg.aberrationIntensity > 0) {
            float ca = cfg.aberrationIntensity;
            float off = displ * 0.10f * ca;

            // Red channel (left-up offset)
            p.reset(); p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(0.45f * d);
            p.setColor(argb((int)(255 * ca * 0.36f), 0xFF5A5A));
            RectF redR = new RectF(rect.left - off, rect.top - off,
                    rect.right - off, rect.bottom - off);
            drawRR(canvas, redR);

            // Green channel (center)
            p.setStrokeWidth(0.3f * d);
            p.setColor(argb((int)(255 * ca * 0.16f), 0x5AFF5A));
            drawRR(canvas, rect);

            // Blue channel (right-down offset)
            p.setStrokeWidth(0.45f * d);
            p.setColor(argb((int)(255 * ca * 0.26f), 0x5A5AFF));
            RectF blueR = new RectF(rect.left + off * 0.8f, rect.top + off * 0.8f,
                    rect.right + off * 0.8f, rect.bottom + off * 0.8f);
            drawRR(canvas, blueR);
        }

        // ═══ Layer 7: Top hairline ═══
        p.reset(); p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(0.25f * d);
        p.setColor(argb((int)(255 * hl * 0.58f), 0xFFFFFF));
        float ty = rect.top + 0.5f * d;
        canvas.drawLine(rect.left + rPx * 0.4f, ty, rect.right - rPx * 0.4f, ty, p);

        // ═══ Layer 8: Bottom shadow ═══
        float sh = cfg.bottomShadowIntensity;
        if (sh > 0) {
            p.reset(); p.setAlpha(255);
            p.setShader(new LinearGradient(rect.left, rect.bottom - rPx * 1.5f, rect.left, rect.bottom + 1,
                    argb(0, 0x000000),
                    argb((int)(255 * sh * 0.65f), 0x060E1A),
                    Shader.TileMode.CLAMP));
            canvas.drawRoundRect(rect, rPx, rPx, p);
        }

        // ═══ Layer 9: Noise grain ═══
        if (cfg.enableNoise && cfg.noiseIntensity > 0) {
            p.reset(); p.setColor(argb((int)(255 * cfg.noiseIntensity), 0xFFFFFF));
            int step = Math.max(2, (int)(rPx * 0.14f));
            Random r = new Random(42L);
            for (float x = rect.left + step; x < rect.right; x += step)
                for (float y = rect.top + step; y < rect.bottom; y += step)
                    if (r.nextFloat() > 0.55f)
                        canvas.drawPoint(x + r.nextFloat() * step, y + r.nextFloat() * step, p);
        }

        canvas.restoreToCount(save);

        // ═══ Layer 10: Border gradients (screen + overlay blend) ═══
        float rr = cfg.cornerRadii != null ? cfg.cornerRadii[0] : rPx;
        Path bp = new Path();
        if (cfg.cornerRadii != null) bp.addRoundRect(rect, cfg.cornerRadii, Path.Direction.CW);
        else bp.addRoundRect(rect, rr, rr, Path.Direction.CW);

        // Screen blend outer rim
        p.reset(); p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.0f * d);
        p.setColor(argb((int)(255 * edgeA * 0.50f), 0xFFFFFF));
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
        canvas.drawPath(bp, p);
        p.setXfermode(null);

        // Inner overlay rim
        p.setStrokeWidth(0.55f * d);
        p.setColor(argb((int)(255 * edgeA * 0.32f), 0xFFFFFF));
        RectF inner = new RectF(rect.left + 0.6f*d, rect.top + 0.6f*d,
                rect.right - 0.6f*d, rect.bottom - 0.6f*d);
        Path ip = new Path(); ip.addRoundRect(inner, rr, rr, Path.Direction.CW);
        canvas.drawPath(ip, p);

        // Final solid stroke
        p.setStrokeWidth(1.2f * d);
        p.setColor(cfg.strokeColor);
        canvas.drawPath(bp, p);
    }

    private void drawRR(Canvas c, RectF r) {
        Path path = new Path();
        float rr = cfg.cornerRadii != null ? cfg.cornerRadii[0] : rPx;
        path.addRoundRect(r, rr, rr, Path.Direction.CW);
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
