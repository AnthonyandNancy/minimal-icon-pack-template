package com.template.iconpack.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/**
 * Dark-toned liquid-glass background with 4 blobs.
 * Colors: #9FB5D1 → #91C7C9 → #7FB5C0
 */
public class LiquidBackgroundView extends View {

    private final Paint bgPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint blobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final int GRAD_TOP    = 0xFF9FB5D1;
    private static final int GRAD_MID    = 0xFF91C7C9;
    private static final int GRAD_BOTTOM = 0xFF7FB5C0;

    private static final int BLOB_BLUE   = 0xFF5EA8FF;
    private static final int BLOB_PURPLE = 0xFF8E6CFF;
    private static final int BLOB_CYAN   = 0xFF25D6C8;
    private static final int BLOB_DARK   = 0xFF3D6173;

    public LiquidBackgroundView(Context context) { super(context); }
    public LiquidBackgroundView(Context context, AttributeSet attrs) { super(context, attrs); }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth(), h = getHeight();
        if (w == 0 || h == 0) return;

        // Linear gradient base
        bgPaint.setShader(new LinearGradient(0, 0, 0, h,
                GRAD_TOP, GRAD_BOTTOM, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, bgPaint);

        float r;

        // Blob 1: top-left blue
        r = Math.min(w, h) * 0.65f;
        blob(canvas, w * 0.18f, h * 0.10f, r, BLOB_BLUE, 0.30f);

        // Blob 2: top-right purple
        r = Math.min(w, h) * 0.60f;
        blob(canvas, w * 0.88f, h * 0.16f, r, BLOB_PURPLE, 0.28f);

        // Blob 3: centre cyan
        r = Math.min(w, h) * 0.70f;
        blob(canvas, w * 0.45f, h * 0.55f, r, BLOB_CYAN, 0.22f);

        // Blob 4: bottom dark
        r = Math.min(w, h) * 0.85f;
        blob(canvas, w * 0.5f, h * 1.05f, r, BLOB_DARK, 0.18f);
    }

    private void blob(Canvas c, float cx, float cy, float r, int color, float alpha) {
        blobPaint.setShader(new RadialGradient(cx, cy, r,
                argb(color, alpha), argb(color, 0f), Shader.TileMode.CLAMP));
        c.drawCircle(cx, cy, r, blobPaint);
    }

    private static int argb(int color, float alpha) {
        return ((int)(255 * alpha) << 24) | (color & 0x00FFFFFF);
    }
}
