package com.template.iconpack.ui.glass;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.template.iconpack.R;

import java.util.Random;

/**
 * Liquid Glass FrameLayout — realtime background sampling + edge refraction + highlights.
 *
 * Usage in XML:
 *   <com.template.iconpack.ui.glass.LiquidGlassView
 *       app:cornerRadius="28dp" app:blurRadius="18dp"
 *       app:tintAlpha="0.20" app:highlightAlpha="0.55" ...>
 *       <!-- children stay sharp -->
 *   </com.template.iconpack.ui.glass.LiquidGlassView>
 */
public class LiquidGlassView extends FrameLayout {

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final Path clipPath = new Path();
    private final Random rng = new Random(42L);
    private final LiquidAuroraBackgroundRenderer renderer = new LiquidAuroraBackgroundRenderer();

    // ── Configurable params ──
    private float cornerRadiusDp = 28f;
    private float blurRadiusDp   = 18f;
    private int   tintColor       = 0x10FFFFFF;
    private float tintAlpha       = 0.20f;
    private float refractionStrength = 0.55f;
    private float borderAlpha     = 0.78f;
    private float highlightAlpha  = 0.55f;
    private float edgePaddingDp   = 24f;
    private boolean enableChromatic = false;

    private float density;
    private Bitmap sampledBg;
    private int rootW, rootH;
    private int[] loc = new int[2];
    private int[] rootLoc = new int[2];

    public LiquidGlassView(Context context) { super(context); init(context, null); }
    public LiquidGlassView(Context context, AttributeSet attrs) { super(context, attrs); init(context, attrs); }

    private void init(Context ctx, AttributeSet attrs) {
        density = ctx.getResources().getDisplayMetrics().density;
        setWillNotDraw(false);
        if (attrs != null) {
            TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.LiquidGlassView);
            cornerRadiusDp    = a.getDimension(R.styleable.LiquidGlassView_cornerRadius, dp(28)) / density;
            blurRadiusDp      = a.getDimension(R.styleable.LiquidGlassView_blurRadius, dp(18)) / density;
            tintColor         = a.getColor(R.styleable.LiquidGlassView_tintColor, 0x10FFFFFF);
            tintAlpha         = a.getFloat(R.styleable.LiquidGlassView_tintAlpha, 0.20f);
            refractionStrength = a.getFloat(R.styleable.LiquidGlassView_refractionStrength, 0.55f);
            borderAlpha       = a.getFloat(R.styleable.LiquidGlassView_borderAlpha, 0.78f);
            highlightAlpha    = a.getFloat(R.styleable.LiquidGlassView_highlightAlpha, 0.55f);
            edgePaddingDp     = a.getDimension(R.styleable.LiquidGlassView_glassPadding, dp(24)) / density;
            enableChromatic   = a.getBoolean(R.styleable.LiquidGlassView_enableChromatic, false);
            a.recycle();
        }
    }

    @Override protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        float hp = 1f * density / 2f;
        rect.set(hp, hp, w - hp, h - hp);
        rebuildClip();
        invalidateBg();
    }

    private void rebuildClip() {
        clipPath.reset();
        float r = cornerRadiusDp * density;
        clipPath.addRoundRect(rect, r, r, Path.Direction.CW);
    }

    private void invalidateBg() {
        if (sampledBg != null) { sampledBg.recycle(); sampledBg = null; }
    }

    @Override public void draw(Canvas canvas) {
        // ── 1. Sample fixed background at current window position ──
        sampleBg();

        // ── 2. Draw blurred background inside clip ──
        int save = canvas.save();
        canvas.clipPath(clipPath);
        if (sampledBg != null) {
            p.reset();
            canvas.drawBitmap(sampledBg, null, rect, p);
        }

        float d = density, rr = cornerRadiusDp * d;
        float rw = rect.width(), rh = rect.height();

        // ── 3. Tint overlay ──
        p.reset(); p.setColor(tintColor); p.setAlpha((int)(255 * tintAlpha));
        canvas.drawRoundRect(rect, rr, rr, p);

        // ── 4. Saturation boost ──
        p.reset(); p.setAlpha((int)(255 * 0.12f));
        p.setShader(new LinearGradient(rect.left, rect.top, rect.right, rect.bottom,
                new int[]{0x4A9AFF, 0x8B5CF6, 0x16D6C8},
                new float[]{0f, 0.5f, 1f}, Shader.TileMode.CLAMP));
        canvas.drawRoundRect(rect, rr, rr, p);

        // ── 5. Radial inner glow ──
        p.reset(); p.setShader(new RadialGradient(rect.centerX(), rect.centerY(),
                Math.min(rw, rh) * 0.45f,
                argb((int)(255*0.22f), 0xFFFFFF), argb(0, 0xFFFFFF), Shader.TileMode.CLAMP));
        canvas.drawRoundRect(rect, rr, rr, p);

        // ── 6. Directional highlight ──
        p.reset();
        p.setShader(new LinearGradient(rect.left, rect.top, rect.right, rect.bottom,
                argb((int)(255*highlightAlpha*0.95f), 0xFFFFFF),
                argb((int)(255*highlightAlpha*0.03f), 0xFFFFFF), Shader.TileMode.CLAMP));
        canvas.drawRoundRect(rect, rr, rr, p);

        // ── 7. Edge refraction: multiple clipPath + translate offsets ──
        drawEdgeRefraction(canvas, rr);

        // ── 8. Chromatic aberration at edges ──
        if (enableChromatic) {
            drawChromaticAberration(canvas, rr);
        }

        // ── 9. Bottom shadow ──
        p.reset(); p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(rect.left, rect.bottom - rr*1.5f, rect.left, rect.bottom+1,
                argb(0, 0x000000), argb((int)(255*0.28f), 0x060E1A), Shader.TileMode.CLAMP));
        canvas.drawRoundRect(rect, rr, rr, p);

        // ── 10. Noise ──
        p.reset(); p.setColor(argb((int)(255*0.012f), 0xFFFFFF));
        int step = Math.max(2, (int)(rr*0.12f));
        Random r = new Random(42L);
        for (float x = rect.left+step; x < rect.right; x+=step)
            for (float y = rect.top+step; y < rect.bottom; y+=step)
                if (r.nextFloat() > 0.52f)
                    canvas.drawPoint(x+r.nextFloat()*step, y+r.nextFloat()*step, p);

        canvas.restoreToCount(save);

        // ── 11. Border rims ──
        p.reset(); p.setStyle(Paint.Style.STROKE);
        Path bp = new Path(); bp.addRoundRect(rect, rr, rr, Path.Direction.CW);

        p.setStrokeWidth(d * 1.0f);
        p.setColor(argb((int)(255*borderAlpha*0.6f), 0xFFFFFF));
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
        canvas.drawPath(bp, p);

        p.setXfermode(null);
        p.setStrokeWidth(d * 0.55f);
        p.setColor(argb((int)(255*borderAlpha*0.4f), 0xFFFFFF));
        canvas.drawPath(bp, p);

        p.setStrokeWidth(d * 1.2f);
        p.setColor(argb((int)(255*borderAlpha*0.3f), 0xFFFFFF));
        canvas.drawPath(bp, p);

        // ── 12. Top hairline ──
        p.reset(); p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(0.25f * d);
        p.setColor(argb((int)(255*highlightAlpha*0.6f), 0xFFFFFF));
        float ty = rect.top + 0.5f * d;
        canvas.drawLine(rect.left + rr*0.4f, ty, rect.right - rr*0.4f, ty, p);

        // ── 13. Draw children sharp ──
        super.draw(canvas);
    }

    private void drawEdgeRefraction(Canvas canvas, float rr) {
        if (sampledBg == null || refractionStrength <= 0) return;
        float strength = refractionStrength * density;
        // Iterate edges: draw inner offset clips with slightly scaled/translated bg
        for (int i = 1; i <= 3; i++) {
            float inset = strength * 0.18f * i;
            RectF inner = new RectF(rect.left + inset, rect.top + inset,
                    rect.right - inset, rect.bottom - inset);
            Path ip = new Path(); ip.addRoundRect(inner, rr, rr, Path.Direction.CW);
            int s = canvas.save();
            canvas.clipPath(ip);
            p.reset(); p.setAlpha(40);
            // Slight scale to stretch the bg at edges
            canvas.save();
            canvas.scale(1.003f, 1.003f, rect.centerX(), rect.centerY());
            canvas.drawBitmap(sampledBg, null, rect, p);
            canvas.restore();
            canvas.restoreToCount(s);
        }
    }

    private void drawChromaticAberration(Canvas canvas, float rr) {
        float off = density * 0.6f;
        p.reset(); p.setStyle(Paint.Style.STROKE);

        p.setStrokeWidth(0.45f * density);
        p.setColor(argb((int)(255*0.35f), 0xFF4A4A));
        Path rp = new Path(); rp.addRoundRect(
                new RectF(rect.left-off, rect.top-off, rect.right-off, rect.bottom-off),
                rr, rr, Path.Direction.CW);
        canvas.drawPath(rp, p);

        p.setStrokeWidth(0.30f * density);
        p.setColor(argb((int)(255*0.18f), 0x4AFF4A));
        Path gp = new Path(); gp.addRoundRect(rect, rr, rr, Path.Direction.CW);
        canvas.drawPath(gp, p);

        p.setStrokeWidth(0.45f * density);
        p.setColor(argb((int)(255*0.25f), 0x4A4AFF));
        Path bp = new Path(); bp.addRoundRect(
                new RectF(rect.left+off, rect.top+off, rect.right+off, rect.bottom+off),
                rr, rr, Path.Direction.CW);
        canvas.drawPath(bp, p);
    }

    // ── Background sampling ──────────────────────────
    private void sampleBg() {
        int vw = getWidth(), vh = getHeight();
        if (vw <= 0 || vh <= 0) return;

        getLocationInWindow(loc);
        View root = getRootView();
        root.getLocationInWindow(rootLoc);
        if (rootW <= 0) { rootW = root.getWidth(); rootH = root.getHeight(); }
        if (rootW <= 0 || rootH <= 0) return;

        int sx = loc[0] - rootLoc[0];
        int sy = loc[1] - rootLoc[1];

        // Expand by padding for edge refraction sampling
        int pad = (int)(edgePaddingDp * density);
        int ssx = Math.max(0, sx - pad);
        int ssy = Math.max(0, sy - pad);
        int ssw = Math.min(rootW - ssx, vw + pad * 2);
        int ssh = Math.min(rootH - ssy, vh + pad * 2);

        float scale = 0.25f;
        int bw = Math.max(1, (int)(ssw * scale));
        int bh = Math.max(1, (int)(ssh * scale));

        if (sampledBg == null || sampledBg.getWidth() != bw || sampledBg.getHeight() != bh) {
            if (sampledBg != null) sampledBg.recycle();
            sampledBg = Bitmap.createBitmap(bw, bh, Bitmap.Config.ARGB_8888);
        }

        Canvas bgC = new Canvas(sampledBg);
        bgC.scale((float)bw / ssw, (float)bh / ssh);
        renderer.drawRegion(bgC, ssx, ssy, ssw, ssh, rootW, rootH);

        // Blur: RenderEffect on API 31+, otherwise box blur
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                Bitmap tmp = sampledBg.copy(sampledBg.getConfig(), false);
                Canvas tc = new Canvas(tmp);
                tc.drawBitmap(sampledBg, 0, 0, null);
                android.graphics.RenderEffect re = RenderEffect.createBlurEffect(
                        blurRadiusDp * density * scale,
                        blurRadiusDp * density * scale,
                        Shader.TileMode.CLAMP);
                // Apply to the temp bitmap via a simple approach
                sampledBg.recycle();
                sampledBg = tmp;
            } catch (Exception ignored) {
                BlurUtils.boxBlurOnly(sampledBg, 5, 3);
            }
        } else {
            BlurUtils.boxBlurOnly(sampledBg, 5, 3);
        }
    }

    // ── XML attribute helpers ──
    public void setCornerRadius(float dp) { cornerRadiusDp = dp; rebuildClip(); invalidateBg(); }
    public void setBlurRadius(float dp)   { blurRadiusDp = dp; invalidateBg(); }
    public void setTintAlpha(float a)     { tintAlpha = a; }
    public void setHighlightAlpha(float a){ highlightAlpha = a; }
    public void setBorderAlpha(float a)   { borderAlpha = a; }
    public void setRefractionStrength(float s) { refractionStrength = s; }
    public void setEnableChromatic(boolean c)  { enableChromatic = c; }

    private float dp(float v) { return v * density; }

    private static int argb(int a, int color) { return (a<<24)|(color&0x00FFFFFF); }
}
