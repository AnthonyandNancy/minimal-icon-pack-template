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
 * 1:1 replica of liquid-glass-react.
 *
 * Layers (order = paint order, back to front):
 *   0  blurred BG          backdrop-filter: blur()
 *   1  glass tint          baseColor × opacity
 *   2  saturation wash     blue→purple→cyan gradient (saturate)
 *   3  inner radial glow   center-bright radial gradient
 *   4  directional light   top-left→bottom-right white linear gradient
 *   5  edge displacement   1.2dp bright stroke + 0.45dp offset dark stroke
 *   6  chromatic aberration R↑left / G centre / B↓right coloured edge strokes
 *   7  top hairline        0.28dp white line at top edge
 *   8  bottom shadow       dark gradient from bottom 30%
 *   9  noise grain         fixed-seed random dots
 *  10a screen rim          0.9dp white stroke (SCREEN blend)
 *  10b overlay rim         0.5dp white stroke at inset 0.6dp
 *  10c solid rim           1.2dp strokeColor
 */
public class LiquidGlassDrawable extends Drawable {

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private final GlassMaterialConfig cfg;
    private final float d, rPx;

    private Bitmap blurredBg;
    private boolean bgCaptured;

    public LiquidGlassDrawable(GlassMaterialConfig config, float density) {
        this.cfg = config; this.d = density;
        this.rPx = config.cornerRadiusDp * d;
    }

    // auto-detect host view from callback
    private View detectView() {
        Drawable.Callback cb = getCallback();
        return (cb instanceof View) ? (View) cb : null;
    }

    @Override protected void onBoundsChange(android.graphics.Rect b) {
        super.onBoundsChange(b);
        float h = 1.2f * d / 2f;
        rect.set(b.left + h, b.top + h, b.right - h, b.bottom - h);
        bgCaptured = false; // re-capture on resize
    }

    @Override public void draw(Canvas canvas) {
        float rr = cfg.cornerRadii != null ? cfg.cornerRadii[0] : rPx;
        Path clipPath = new Path();
        if (cfg.cornerRadii != null) clipPath.addRoundRect(rect, cfg.cornerRadii, Path.Direction.CW);
        else clipPath.addRoundRect(rect, rr, rr, Path.Direction.CW);

        int save = canvas.save();
        canvas.clipPath(clipPath);

        float alpha = cfg.opacity;
        float hl = cfg.highlightIntensity;
        float edge = cfg.edgeIntensity;
        float sh = cfg.bottomShadowIntensity;
        float displ = cfg.displacementScale * d;

        // ═══════════════════════════════════════════════════════
        //  0: backdrop-filter: blur() — real background sampling
        // ═══════════════════════════════════════════════════════
        if (!bgCaptured) captureBg();
        if (blurredBg != null) {
            p.reset();
            canvas.drawBitmap(blurredBg, rect.left, rect.top, p);
        }

        // ═══════ 1: glass tint ═══════
        p.reset();
        p.setColor(cfg.baseColor);
        p.setAlpha((int)(255 * alpha));
        fillRR(canvas, rect, rr);

        // ═══════ 2: saturation colour wash ═══════
        float satA = cfg.saturation * cfg.innerGlowIntensity * 5f;
        if (satA > 0.002f) {
            p.reset();
            p.setAlpha((int)(255 * Math.min(satA, 0.25f)));
            p.setShader(new LinearGradient(rect.left, rect.top, rect.right, rect.bottom,
                    new int[]{0x4A9AFF, 0x8B5CF6, 0x16D6C8},
                    new float[]{0f, 0.5f, 1f}, Shader.TileMode.CLAMP));
            fillRR(canvas, rect, rr);
        }

        // ═══════ 3: inner radial glow ═══════
        float glowA = cfg.innerGlowIntensity * 2.8f;
        if (glowA > 0) {
            p.reset();
            float dim = Math.min(rect.width(), rect.height());
            p.setShader(new RadialGradient(rect.centerX(), rect.centerY(), dim * 0.50f,
                    argb((int)(255 * glowA * 0.58f), 0xFFFFFF),
                    argb(0, 0xFFFFFF), Shader.TileMode.CLAMP));
            fillRR(canvas, rect, rr);
        }

        // ═══════ 4: directional highlight ═══════
        if (hl > 0) {
            p.reset();
            p.setShader(new LinearGradient(rect.left, rect.top, rect.right, rect.bottom,
                    argb((int)(255 * hl * 0.98f), 0xFFFFFF),
                    argb((int)(255 * hl * 0.04f), 0xFFFFFF),
                    Shader.TileMode.CLAMP));
            fillRR(canvas, rect, rr);
        }

        // ═══════ 5: edge displacement ═══════
        p.reset();
        p.setStyle(Paint.Style.STROKE);

        p.setStrokeWidth(1.2f * d);
        p.setColor(argb((int)(255 * edge * 0.85f), 0xFFFFFF));
        strokeRR(canvas, rect, rr);

        p.setStrokeWidth(0.45f * d);
        p.setColor(argb((int)(255 * edge * 0.28f), 0x1A2A40));
        strokeRR(canvas, margin(rect, displ * 0.35f), rr);

        // ═══════ 6: chromatic aberration ═══════
        if (cfg.enableChromaticAberration && cfg.aberrationIntensity > 0) {
            float ca = cfg.aberrationIntensity;
            float off = displ * 0.14f * ca;

            p.setStrokeWidth(0.5f * d);
            p.setColor(argb((int)(255 * ca * 0.44f), 0xFF4A4A));
            strokeRR(canvas, margin(rect, -off), rr);

            p.setStrokeWidth(0.32f * d);
            p.setColor(argb((int)(255 * ca * 0.22f), 0x4AFF4A));
            strokeRR(canvas, rect, rr);

            p.setStrokeWidth(0.5f * d);
            p.setColor(argb((int)(255 * ca * 0.32f), 0x4A4AFF));
            strokeRR(canvas, margin(rect, off * 0.75f), rr);
        }

        // ═══════ 7: top hairline ═══════
        p.reset();
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(0.28f * d);
        p.setColor(argb((int)(255 * hl * 0.68f), 0xFFFFFF));
        float ty = rect.top + 0.6f * d;
        canvas.drawLine(rect.left + rr * 0.45f, ty, rect.right - rr * 0.45f, ty, p);

        // ═══════ 8: bottom shadow ═══════
        if (sh > 0) {
            p.reset();
            p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(rect.left, rect.bottom - rPx * 1.8f,
                    rect.left, rect.bottom + 1,
                    argb(0, 0x000000),
                    argb((int)(255 * sh * 0.75f), 0x0A1628),
                    Shader.TileMode.CLAMP));
            fillRR(canvas, rect, rr);
        }

        // ═══════ 9: noise grain ═══════
        if (cfg.enableNoise && cfg.noiseIntensity > 0) {
            p.reset();
            p.setColor(argb((int)(255 * cfg.noiseIntensity), 0xFFFFFF));
            int step = Math.max(2, (int)(rPx * 0.14f));
            Random r = new Random(42L);
            for (float x = rect.left + step; x < rect.right; x += step)
                for (float y = rect.top + step; y < rect.bottom; y += step)
                    if (r.nextFloat() > 0.52f)
                        canvas.drawPoint(x + r.nextFloat() * step, y + r.nextFloat() * step, p);
        }

        canvas.restoreToCount(save);

        // ═══════ 10: border rims ═══════
        Path bp = new Path();
        if (cfg.cornerRadii != null) bp.addRoundRect(rect, cfg.cornerRadii, Path.Direction.CW);
        else bp.addRoundRect(rect, rr, rr, Path.Direction.CW);

        p.reset();
        p.setStyle(Paint.Style.STROKE);

        // 10a: screen blend outer glow
        p.setStrokeWidth(d * 0.9f);
        p.setColor(argb((int)(255 * edge * 0.58f), 0xFFFFFF));
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
        canvas.drawPath(bp, p);
        p.setXfermode(null);

        // 10b: overlay inner rim
        p.setStrokeWidth(d * 0.5f);
        p.setColor(argb((int)(255 * edge * 0.40f), 0xFFFFFF));
        RectF inr = new RectF(rect.left + d * 0.6f, rect.top + d * 0.6f,
                rect.right - d * 0.6f, rect.bottom - d * 0.6f);
        Path inp = new Path();
        inp.addRoundRect(inr, rr, rr, Path.Direction.CW);
        canvas.drawPath(inp, p);

        // 10c: solid stroke
        p.setStrokeWidth(1.2f * d);
        p.setColor(cfg.strokeColor);
        canvas.drawPath(bp, p);
    }

    // ── background blur capture ──────────────────────
    private void captureBg() {
        View v = detectView();
        if (v == null || v.getWidth() <= 0 || v.getHeight() <= 0) return;
        // Get background view from the view hierarchy root
        View root = v.getRootView();
        View bgView = root.findViewById(com.template.iconpack.R.id.liquid_bg);
        if (bgView instanceof com.template.iconpack.ui.views.LiquidBackgroundView) {
            com.template.iconpack.ui.views.LiquidBackgroundView lbg =
                    (com.template.iconpack.ui.views.LiquidBackgroundView) bgView;
            int[] loc = new int[2];
            v.getLocationOnScreen(loc);
            blurredBg = lbg.getBlurredBackdrop(loc[0], loc[1], v.getWidth(), v.getHeight());
        }
        if (blurredBg != null) bgCaptured = true;
    }

    // ── draw helpers ─────────────────────────────────
    private void fillRR(Canvas c, RectF r, float rr) {
        p.setStyle(Paint.Style.FILL);
        c.drawRoundRect(r, rr, rr, p);
    }
    private void strokeRR(Canvas c, RectF r, float rr) {
        Path path = new Path();
        path.addRoundRect(r, rr, rr, Path.Direction.CW);
        c.drawPath(path, p);
    }
    private static RectF margin(RectF r, float m) {
        return new RectF(r.left + m, r.top + m, r.right - m, r.bottom - m);
    }

    @Override public void setAlpha(int a) {}
    @Override public void setColorFilter(ColorFilter f) {}
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    private static int argb(int a, int color) { return (a << 24) | (color & 0x00FFFFFF); }
}
