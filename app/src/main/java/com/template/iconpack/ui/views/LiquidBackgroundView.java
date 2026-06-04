package com.template.iconpack.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import com.template.iconpack.ui.GlassTheme;

/**
 * Dark-toned layered background: gradient base + 4 soft light blobs.
 * Colors are slightly muted so semi-transparent glass cards stand out.
 */
public class LiquidBackgroundView extends View {

    private final Paint bgPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint blobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public LiquidBackgroundView(Context context) { super(context); }
    public LiquidBackgroundView(Context context, AttributeSet attrs) { super(context, attrs); }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth(), h = getHeight();
        if (w == 0 || h == 0) return;

        // 1. Linear gradient base (darker blue-grey → purple → cyan)
        bgPaint.setShader(new LinearGradient(0, 0, 0, h,
                GlassTheme.BG_TOP, GlassTheme.BG_BOTTOM, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, bgPaint);

        float r;

        // 2. Blob: top-left (blue)
        r = Math.min(w, h) * 0.60f;
        drawBlob(canvas, w * 0.10f, h * 0.06f, r,
                GlassTheme.BLOB_BLUE, GlassTheme.BLOB_BLUE_ALPHA);

        // 3. Blob: top-right (purple)
        r = Math.min(w, h) * 0.55f;
        drawBlob(canvas, w * 0.88f, h * 0.14f, r,
                GlassTheme.BLOB_PURPLE, GlassTheme.BLOB_PURPLE_ALPHA);

        // 4. Blob: centre-bottom (cyan)
        r = Math.min(w, h) * 0.65f;
        drawBlob(canvas, w * 0.48f, h * 0.68f, r,
                GlassTheme.BLOB_CYAN, GlassTheme.BLOB_CYAN_ALPHA);

        // 5. Soft dark shading bottom
        r = Math.min(w, h) * 0.70f;
        drawBlob(canvas, w * 0.50f, h * 0.90f, r,
                GlassTheme.BLOB_DARK, GlassTheme.BLOB_DARK_ALPHA);
    }

    private void drawBlob(Canvas c, float cx, float cy, float r, int color, float alpha) {
        blobPaint.setShader(new RadialGradient(cx, cy, r,
                argb(color, alpha), argb(color, 0f), Shader.TileMode.CLAMP));
        c.drawCircle(cx, cy, r, blobPaint);
    }

    private static int argb(int color, float alpha) {
        return ((int)(255 * alpha) << 24) | (color & 0x00FFFFFF);
    }
}
