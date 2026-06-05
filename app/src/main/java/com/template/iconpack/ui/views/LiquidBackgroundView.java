package com.template.iconpack.ui.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.template.iconpack.ui.glass.BlurUtils;

/**
 * iOS 26 wallpaper background.
 * Exposes a blurred version of itself for LiquidGlassDrawable to use as backdrop.
 */
public class LiquidBackgroundView extends View {

    private final Paint bgPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint blobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final int[] COLORS = {
        0xFFF2EBE5, 0xFFEBE0F0, 0xFFE5E8F5, 0xFFE8F0F5, 0xFFF0EBE5
    };
    private static final float[] STOPS = {0f, 0.3f, 0.55f, 0.78f, 1f};

    private static final int[]   BC = {
        0xFFA8D8EA, 0xFFC9B8E8, 0xFFF5C4B8, 0xFFB8E8D0, 0xFFF5E4B8, 0xFFF5C4D8
    };
    private static final float[] BA = {0.38f, 0.32f, 0.34f, 0.28f, 0.30f, 0.26f};
    private static final float[] BS = {0.48f, 0.52f, 0.44f, 0.50f, 0.46f, 0.42f};
    private static final float[] BX = {0.12f, 0.82f, 0.48f, 0.22f, 0.72f, 0.58f};
    private static final float[] BY = {0.18f, 0.32f, 0.58f, 0.78f, 0.48f, 0.82f};
    private static final float[] BP = {0f, 1.05f, 2.09f, 3.14f, 4.19f, 5.24f};

    private float animT;
    private ValueAnimator animator;
    private boolean attached;

    // Cached blurred version for glass components
    private Bitmap blurredCache;
    private boolean needsRecache = true;

    public LiquidBackgroundView(Context c) { super(c); }
    public LiquidBackgroundView(Context c, AttributeSet a) { super(c, a); }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!attached) {
            attached = true;
            animator = ValueAnimator.ofFloat(0f, (float)(2 * Math.PI));
            animator.setDuration(40000);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setInterpolator(new LinearInterpolator());
            animator.addUpdateListener(a -> {
                animT = (float)a.getAnimatedValue();
                needsRecache = true; // recache blurred bg on each frame
                invalidate();
            });
            animator.start();
        }
    }

    @Override protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null) { animator.cancel(); animator = null; }
        if (blurredCache != null) { blurredCache.recycle(); blurredCache = null; }
        attached = false;
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth(), h = getHeight();
        if (w == 0 || h == 0) return;

        bgPaint.setShader(new LinearGradient(0, 0, w, h, COLORS, STOPS, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, bgPaint);

        float s = Math.min(w, h);
        for (int i = 0; i < 6; i++) {
            float ox = (float)Math.sin(animT * 0.6f + BP[i]) * 0.03f * w;
            float oy = (float)Math.cos(animT * 0.45f + BP[i]) * 0.025f * h;
            blob(canvas, w * BX[i] + ox, h * BY[i] + oy, s * BS[i], BC[i], BA[i]);
        }
    }

    private void blob(Canvas c, float x, float y, float r, int col, float a) {
        blobPaint.setShader(new RadialGradient(x, y, r,
                argb(col, a), argb(col, 0f), Shader.TileMode.CLAMP));
        c.drawCircle(x, y, r, blobPaint);
    }

    /** Get a blurred copy of the background for glass components. */
    public Bitmap getBlurredBackdrop(int viewLeft, int viewTop, int viewW, int viewH) {
        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return null;

        // Re-render to a scaled-down bitmap and blur
        if (needsRecache || blurredCache == null) {
            float scale = 0.25f;
            int sw = Math.max(1, (int)(w * scale));
            int sh = Math.max(1, (int)(h * scale));

            Bitmap small = Bitmap.createBitmap(sw, sh, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(small);
            c.scale(scale, scale);
            super.draw(c);

            // Blur the small version
            blurredCache = BlurUtils.boxBlurOnly(small, 8, 2);
            if (blurredCache != small) small.recycle();
            needsRecache = false;
        }

        // Crop the portion under the glass view
        int bw = blurredCache.getWidth(), bh = blurredCache.getHeight();
        float scale = (float) bw / w;
        int sx = Math.max(0, Math.min(bw, (int)(viewLeft * scale)));
        int sy = Math.max(0, Math.min(bh, (int)(viewTop * scale)));
        int cropW = Math.min(bw - sx, Math.max(1, (int)(viewW * scale)));
        int cropH = Math.min(bh - sy, Math.max(1, (int)(viewH * scale)));

        try {
            return Bitmap.createBitmap(blurredCache, sx, sy, cropW, cropH);
        } catch (Exception e) {
            return null;
        }
    }

    private static int argb(int c, float a) { return ((int)(255*a)<<24)|(c&0x00FFFFFF); }
}
