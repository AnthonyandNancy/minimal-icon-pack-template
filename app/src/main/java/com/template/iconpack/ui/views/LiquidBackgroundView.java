package com.template.iconpack.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import com.template.iconpack.ui.GlassTheme;

/**
 * Background view that renders soft gradient blobs for Harmony-style Liquid Glass.
 * Five blobs: top-left blue, top-right purple, center soft cyan, mid-right blue, bottom cyan.
 */
public class LiquidBackgroundView extends View {

    private final Paint blobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public LiquidBackgroundView(Context context) {
        super(context);
    }

    public LiquidBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;

        float cx, cy, radius;

        // Blob 1: top-left (blue)
        cx = w * 0.12f;
        cy = h * 0.08f;
        radius = Math.min(w, h) * 0.42f;
        drawBlob(canvas, cx, cy, radius,
                GlassTheme.BLOB_BLUE, GlassTheme.BLOB_BLUE_ALPHA);

        // Blob 2: top-right (purple)
        cx = w * 0.85f;
        cy = h * 0.18f;
        radius = Math.min(w, h) * 0.36f;
        drawBlob(canvas, cx, cy, radius,
                GlassTheme.BLOB_PURPLE, GlassTheme.BLOB_PURPLE_ALPHA);

        // Blob 3: center-upper (light cyan)
        cx = w * 0.50f;
        cy = h * 0.40f;
        radius = Math.min(w, h) * 0.35f;
        drawBlob(canvas, cx, cy, radius,
                GlassTheme.BLOB_CYAN, GlassTheme.BLOB_CYAN_ALPHA);

        // Blob 4: mid-right (soft blue)
        cx = w * 0.80f;
        cy = h * 0.60f;
        radius = Math.min(w, h) * 0.30f;
        drawBlob(canvas, cx, cy, radius,
                GlassTheme.BLOB_BLUE, GlassTheme.BLOB_BLUE_ALPHA * 0.6f);

        // Blob 5: bottom-left (cyan blend)
        cx = w * 0.25f;
        cy = h * 0.85f;
        radius = Math.min(w, h) * 0.32f;
        drawBlob(canvas, cx, cy, radius,
                GlassTheme.BLOB_CYAN, GlassTheme.BLOB_CYAN_ALPHA * 0.7f);
    }

    private void drawBlob(Canvas canvas, float cx, float cy, float radius,
                          int color, float alpha) {
        blobPaint.setShader(new RadialGradient(
                cx, cy, radius,
                colorWithAlpha(color, alpha),
                colorWithAlpha(color, 0f),
                Shader.TileMode.CLAMP
        ));
        canvas.drawCircle(cx, cy, radius, blobPaint);
    }

    private static int colorWithAlpha(int color, float alpha) {
        int a = (int) (255 * alpha);
        return (a << 24) | (color & 0x00FFFFFF);
    }
}
