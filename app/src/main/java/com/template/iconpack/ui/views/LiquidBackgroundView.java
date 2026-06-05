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
 * Deep Aurora Liquid Glass background.
 * Gradient: #0B1020 → #172554 + 5 floating aurora blobs.
 */
public class LiquidBackgroundView extends View {

    private final Paint bgPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint blobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final int[] COLORS = {0xFF0B1020, 0xFF0E1835, 0xFF111E46, 0xFF172554};
    private static final float[] STOPS = {0f, 0.35f, 0.70f, 1f};

    // Aurora blob definitions: {color, alpha, size, dx, dy, phase}
    private static final int[]   BLOBS_C = {0xFF3B82F6, 0xFF8B5CF6, 0xFFEC4899, 0xFF3B82F6, 0xFF8B5CF6};
    private static final float[] BLOBS_A = {0.26f, 0.22f, 0.18f, 0.20f, 0.16f};
    private static final float[] BLOBS_S = {0.85f, 0.78f, 0.90f, 0.72f, 0.82f};
    private static final float[] BLOBS_X = {0.15f, 0.85f, 0.50f, 0.22f, 0.78f};
    private static final float[] BLOBS_Y = {0.12f, 0.20f, 0.55f, 0.78f, 0.90f};
    private static final float[] BLOBS_P = {0f, 0.63f, 1.26f, 2.51f, 3.77f}; // phase offsets (rad)

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
            animator.setDuration(30000); // 30s cycle
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setInterpolator(new LinearInterpolator());
            animator.addUpdateListener(a -> {
                animT = (float) a.getAnimatedValue();
                invalidate();
            });
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
        for (int i = 0; i < 5; i++) {
            float ox = (float) Math.sin(animT + BLOBS_P[i]) * 0.06f * w;
            float oy = (float) Math.cos(animT * 0.7f + BLOBS_P[i]) * 0.04f * h;
            float r = s * BLOBS_S[i];
            blob(canvas, w * BLOBS_X[i] + ox, h * BLOBS_Y[i] + oy, r,
                    BLOBS_C[i], BLOBS_A[i]);
        }
    }

    private void blob(Canvas c, float x, float y, float r, int col, float a) {
        blobPaint.setShader(new RadialGradient(x, y, r,
                argb(col, a), argb(col, 0f), Shader.TileMode.CLAMP));
        c.drawCircle(x, y, r, blobPaint);
    }

    private static int argb(int c, float a) { return ((int)(255*a)<<24)|(c&0x00FFFFFF); }
}
