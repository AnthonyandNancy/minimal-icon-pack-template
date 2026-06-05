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
 * Liquid glass — samples the FIXED Aurora background based on screen position.
 * Continuously re-samples on scroll → glass color changes as it moves over different blobs.
 */
public class LiquidGlassDrawable extends Drawable {

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private final GlassMaterialConfig cfg;
    private final float d, rPx;

    private Bitmap sampledBg; // re-sampled every frame
    private final LiquidAuroraBackgroundRenderer renderer = new LiquidAuroraBackgroundRenderer();

    // Cached root view dimensions (for bg sampling)
    private int rootW, rootH;

    public LiquidGlassDrawable(GlassMaterialConfig config, float density) {
        this.cfg = config; this.d = density;
        this.rPx = config.cornerRadiusDp * d;
    }

    private View detectView() {
        Drawable.Callback cb = getCallback();
        return (cb instanceof View) ? (View) cb : null;
    }

    @Override protected void onBoundsChange(android.graphics.Rect b) {
        super.onBoundsChange(b);
        float h = 1.2f * d / 2f;
        rect.set(b.left + h, b.top + h, b.right - h, b.bottom - h);
    }

    @Override public void draw(Canvas canvas) {
        // ── Re-sample the fixed background at current screen position ──
        sampleFixedBackground();

        float rr = cfg.cornerRadii != null ? cfg.cornerRadii[0] : rPx;
        Path clipPath = new Path();
        if (cfg.cornerRadii != null) clipPath.addRoundRect(rect, cfg.cornerRadii, Path.Direction.CW);
        else clipPath.addRoundRect(rect, rr, rr, Path.Direction.CW);

        int save = canvas.save();
        canvas.clipPath(clipPath);

        // ═══ Layer 0: blurred background sample ═══
        if (sampledBg != null) {
            p.reset();
            // Scale the sampled bg to fill the rect
            canvas.drawBitmap(sampledBg, null, rect, p);
        }

        float alpha = cfg.opacity;
        float hl    = cfg.highlightIntensity;
        float edge  = cfg.edgeIntensity;
        float sh    = cfg.bottomShadowIntensity;
        float displ = cfg.displacementScale * d;

        // ═══ Layer 1: glass tint ═══
        p.reset(); p.setColor(cfg.baseColor); p.setAlpha((int)(255 * alpha));
        canvas.drawRoundRect(rect, rPx, rPx, p);

        // ═══ Layer 2: saturation colour wash ═══
        float satA = cfg.saturation * cfg.innerGlowIntensity * 5f;
        if (satA > 0.002f) {
            p.reset(); p.setAlpha((int)(255 * Math.min(satA, 0.20f)));
            p.setShader(new LinearGradient(rect.left, rect.top, rect.right, rect.bottom,
                    new int[]{0x4A9AFF, 0x8B5CF6, 0x16D6C8},
                    new float[]{0f, 0.5f, 1f}, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(rect, rPx, rPx, p);
        }

        // ═══ Layer 3: inner glow ═══
        float glowA = cfg.innerGlowIntensity * 2.5f;
        if (glowA > 0) {
            p.reset();
            float dim = Math.min(rect.width(), rect.height());
            p.setShader(new RadialGradient(rect.centerX(), rect.centerY(), dim * 0.48f,
                    argb((int)(255*glowA*0.50f), 0xFFFFFF),
                    argb(0, 0xFFFFFF), Shader.TileMode.CLAMP));
            canvas.drawRoundRect(rect, rPx, rPx, p);
        }

        // ═══ Layer 4: directional highlight ═══
        if (hl > 0) {
            p.reset();
            p.setShader(new LinearGradient(rect.left, rect.top, rect.right, rect.bottom,
                    argb((int)(255*hl*0.95f), 0xFFFFFF),
                    argb((int)(255*hl*0.04f), 0xFFFFFF),
                    Shader.TileMode.CLAMP));
            canvas.drawRoundRect(rect, rPx, rPx, p);
        }

        // ═══ Layer 5: edge displacement ═══
        p.reset(); p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.2f * d);
        p.setColor(argb((int)(255*edge*0.82f), 0xFFFFFF));
        strokeRR(canvas, rect, rr);

        p.setStrokeWidth(0.42f * d);
        p.setColor(argb((int)(255*edge*0.25f), 0x1A2A40));
        strokeRR(canvas, margin(rect, displ * 0.30f), rr);

        // ═══ Layer 6: chromatic aberration ═══
        if (cfg.enableChromaticAberration && cfg.aberrationIntensity > 0) {
            float ca = cfg.aberrationIntensity;
            float off = displ * 0.12f * ca;
            p.setStrokeWidth(0.48f * d);
            p.setColor(argb((int)(255*ca*0.40f), 0xFF4A4A));
            strokeRR(canvas, margin(rect, -off), rr);
            p.setStrokeWidth(0.30f * d);
            p.setColor(argb((int)(255*ca*0.20f), 0x4AFF4A));
            strokeRR(canvas, rect, rr);
            p.setStrokeWidth(0.48f * d);
            p.setColor(argb((int)(255*ca*0.30f), 0x4A4AFF));
            strokeRR(canvas, margin(rect, off*0.7f), rr);
        }

        // ═══ Layer 7: top hairline ═══
        p.reset(); p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(0.26f * d);
        p.setColor(argb((int)(255*hl*0.62f), 0xFFFFFF));
        float ty = rect.top + 0.55f * d;
        canvas.drawLine(rect.left + rr*0.42f, ty, rect.right - rr*0.42f, ty, p);

        // ═══ Layer 8: bottom shadow ═══
        if (sh > 0) {
            p.reset(); p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(rect.left, rect.bottom - rPx*1.6f, rect.left, rect.bottom+1,
                    argb(0, 0x000000),
                    argb((int)(255*sh*0.70f), 0x060E1A),
                    Shader.TileMode.CLAMP));
            canvas.drawRoundRect(rect, rPx, rPx, p);
        }

        // ═══ Layer 9: noise ═══
        if (cfg.enableNoise && cfg.noiseIntensity > 0) {
            p.reset(); p.setColor(argb((int)(255*cfg.noiseIntensity), 0xFFFFFF));
            int step = Math.max(2, (int)(rPx*0.14f));
            Random r = new Random(42L);
            for (float x = rect.left+step; x < rect.right; x += step)
                for (float y = rect.top+step; y < rect.bottom; y += step)
                    if (r.nextFloat() > 0.52f)
                        canvas.drawPoint(x+r.nextFloat()*step, y+r.nextFloat()*step, p);
        }

        canvas.restoreToCount(save);

        // ═══ Layer 10: border rims ═══
        Path bp = new Path();
        if (cfg.cornerRadii != null) bp.addRoundRect(rect, cfg.cornerRadii, Path.Direction.CW);
        else bp.addRoundRect(rect, rr, rr, Path.Direction.CW);

        p.reset(); p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(d * 0.9f);
        p.setColor(argb((int)(255*edge*0.55f), 0xFFFFFF));
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
        canvas.drawPath(bp, p);
        p.setXfermode(null);

        p.setStrokeWidth(d * 0.5f);
        p.setColor(argb((int)(255*edge*0.38f), 0xFFFFFF));
        RectF inr = new RectF(rect.left+d*0.6f, rect.top+d*0.6f, rect.right-d*0.6f, rect.bottom-d*0.6f);
        Path inp = new Path(); inp.addRoundRect(inr, rr, rr, Path.Direction.CW);
        canvas.drawPath(inp, p);

        p.setStrokeWidth(1.2f * d);
        p.setColor(cfg.strokeColor);
        canvas.drawPath(bp, p);
    }

    // ── Background sampling from the FIXED global background ──
    private void sampleFixedBackground() {
        View v = detectView();
        if (v == null || v.getWidth() <= 0 || v.getHeight() <= 0) return;

        View root = v.getRootView();
        if (rootW <= 0) { rootW = root.getWidth(); rootH = root.getHeight(); }
        if (rootW <= 0 || rootH <= 0) return;

        int[] viewLoc = new int[2];
        int[] rootLoc = new int[2];
        v.getLocationInWindow(viewLoc);
        root.getLocationInWindow(rootLoc);

        int sx = viewLoc[0] - rootLoc[0];
        int sy = viewLoc[1] - rootLoc[1];
        int sw = v.getWidth(), sh = v.getHeight();

        float scale = 0.25f;
        int bmW = Math.max(1, (int)(sw * scale));
        int bmH = Math.max(1, (int)(sh * scale));

        if (sampledBg == null || sampledBg.getWidth() != bmW || sampledBg.getHeight() != bmH) {
            if (sampledBg != null) sampledBg.recycle();
            sampledBg = Bitmap.createBitmap(bmW, bmH, Bitmap.Config.ARGB_8888);
        }

        Canvas bgCanvas = new Canvas(sampledBg);
        bgCanvas.scale((float)bmW / sw, (float)bmH / sh);
        renderer.drawRegion(bgCanvas, sx, sy, sw, sh, rootW, rootH);

        // Apply blur to the sampled region
        BlurUtils.boxBlurOnly(sampledBg, 6, 2);
    }

    private void strokeRR(Canvas c, RectF r, float rr) {
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
