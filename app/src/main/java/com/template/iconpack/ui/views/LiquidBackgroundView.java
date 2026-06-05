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

/** Deep Aurora: #0B1020 → #172554 + 3 giant blurred blobs. */
public class LiquidBackgroundView extends View {

    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint blobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final int[] GC = {0xFF0B1020, 0xFF0E1835, 0xFF111E46, 0xFF172554};
    private static final float[] GS = {0f, 0.35f, 0.70f, 1f};

    private static final int[]   BC = {0xFF3B82F6, 0xFF8B5CF6, 0xFFEC4899};
    private static final float[] BA = {0.30f, 0.26f, 0.22f};
    private static final float[] BS = {0.70f, 0.65f, 0.60f};
    private static final float[] BX = {0.15f, 0.80f, 0.50f};
    private static final float[] BY = {0.15f, 0.25f, 0.60f};
    private static final float[] BP = {0f, 2.09f, 4.19f};

    private float animT;
    private ValueAnimator animator;
    private boolean attached;
    private Bitmap blurredCache;
    private boolean needsRecache = true;

    public LiquidBackgroundView(Context c) { super(c); }
    public LiquidBackgroundView(Context c, AttributeSet a) { super(c, a); }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!attached) {
            attached = true;
            animator = ValueAnimator.ofFloat(0f, (float)(2 * Math.PI));
            animator.setDuration(35000);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setInterpolator(new LinearInterpolator());
            animator.addUpdateListener(a -> {
                animT = (float)a.getAnimatedValue();
                needsRecache = true;
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
        bgPaint.setShader(new LinearGradient(0, 0, 0, h, GC, GS, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, bgPaint);
        float s = Math.min(w, h);
        for (int i = 0; i < 3; i++) {
            float ox = (float)Math.sin(animT*0.7f+BP[i])*0.04f*w;
            float oy = (float)Math.cos(animT*0.5f+BP[i])*0.03f*h;
            blob(canvas, w*BX[i]+ox, h*BY[i]+oy, s*BS[i], BC[i], BA[i]);
        }
    }
    private void blob(Canvas c, float x, float y, float r, int col, float a) {
        blobPaint.setShader(new RadialGradient(x, y, r, argb(col,a), argb(col,0f), Shader.TileMode.CLAMP));
        c.drawCircle(x, y, r, blobPaint);
    }
    public Bitmap getBlurredBackdrop(int vl, int vt, int vw, int vh) {
        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return null;
        if (needsRecache || blurredCache == null) {
            float s = 0.25f; int sw = Math.max(1,(int)(w*s)), sh = Math.max(1,(int)(h*s));
            Bitmap sm = Bitmap.createBitmap(sw, sh, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(sm); c.scale(s, s); super.draw(c);
            blurredCache = BlurUtils.boxBlurOnly(sm, 6, 2);
            if (blurredCache != sm) sm.recycle();
            needsRecache = false;
        }
        int bw = blurredCache.getWidth(), bh = blurredCache.getHeight();
        float sc = (float)bw/w;
        int sx = Math.max(0,Math.min(bw,(int)(vl*sc)));
        int sy = Math.max(0,Math.min(bh,(int)(vt*sc)));
        int cw = Math.min(bw-sx, Math.max(1,(int)(vw*sc)));
        int ch = Math.min(bh-sy, Math.max(1,(int)(vh*sc)));
        try { return Bitmap.createBitmap(blurredCache, sx, sy, cw, ch); } catch(Exception e){return null;}
    }
    private static int argb(int c, float a) { return ((int)(255*a)<<24)|(c&0x00FFFFFF); }
}
