package com.template.iconpack.ui.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * iPhone prismatic rainbow background — makes liquid glass visible.
 * 6 colour blobs slowly drifting + base warm gradient.
 */
public class LiquidBackgroundView extends View {

    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint blobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Warm ivory → soft pink gradient base
    private static final int[] COLORS = {0xFFF8F4F0, 0xFFFAE8E0, 0xFFF5E6F0, 0xFFEDF0F8};
    private static final float[] STOPS = {0f, 0.35f, 0.68f, 1f};

    // 6 drifting rainbow blobs
    private static final int[]   BC = {0xFFFF6B6B, 0xFF4ECDC4, 0xFFFFE66D, 0xFFA78BFA, 0xFF4D96FF, 0xFFFF85A2};
    private static final float[] BA = {0.32f, 0.28f, 0.30f, 0.24f, 0.26f, 0.22f};
    private static final float[] BS = {0.55f, 0.50f, 0.48f, 0.52f, 0.45f, 0.40f};
    private static final float[] BX = {0.15f, 0.80f, 0.50f, 0.20f, 0.75f, 0.55f};
    private static final float[] BY = {0.20f, 0.35f, 0.60f, 0.80f, 0.50f, 0.85f};
    private static final float[] BP = {0f, 1.05f, 2.09f, 3.14f, 4.19f, 5.24f};

    private float animT;
    private ValueAnimator animator;
    private boolean attached;

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
            animator.addUpdateListener(a -> { animT = (float)a.getAnimatedValue(); invalidate(); });
            animator.start();
        }
    }

    @Override protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null) { animator.cancel(); animator = null; }
        attached = false;
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth(), h = getHeight();
        if (w == 0 || h == 0) return;

        bgPaint.setShader(new LinearGradient(0, 0, 0, h, COLORS, STOPS, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, bgPaint);

        float s = Math.min(w, h);
        for (int i = 0; i < 6; i++) {
            float ox = (float)Math.sin(animT * 0.7f + BP[i]) * 0.04f * w;
            float oy = (float)Math.cos(animT * 0.5f + BP[i]) * 0.03f * h;
            float r = s * BS[i];
            blob(canvas, w * BX[i] + ox, h * BY[i] + oy, r, BC[i], BA[i]);
        }
    }

    private void blob(Canvas c, float x, float y, float r, int col, float a) {
        blobPaint.setShader(new RadialGradient(x, y, r,
                argb(col, a), argb(col, 0f), Shader.TileMode.CLAMP));
        c.drawCircle(x, y, r, blobPaint);
    }

    private static int argb(int c, float a) { return ((int)(255*a)<<24)|(c&0x00FFFFFF); }
}
