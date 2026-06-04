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
 * Dark liquid glass background — #101827 → #0B111D.
 * 4 blobs on top.
 */
public class LiquidBackgroundView extends View {

    private final Paint bgPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint blobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final int COLOR_TOP    = 0xFF101827;
    private static final int COLOR_MID1   = 0xFF18233A;
    private static final int COLOR_MID2   = 0xFF1B2E3A;
    private static final int COLOR_BOTTOM = 0xFF0B111D;

    private static final int BLOB_BLUE   = 0xFF2F7DFF;
    private static final int BLOB_PURPLE = 0xFF7C4DFF;
    private static final int BLOB_CYAN   = 0xFF1DD7C2;
    private static final int BLOB_BLACK  = 0xFF000000;

    public LiquidBackgroundView(Context c) { super(c); }
    public LiquidBackgroundView(Context c, AttributeSet a) { super(c, a); }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth(), h = getHeight();
        if (w == 0 || h == 0) return;

        bgPaint.setShader(new LinearGradient(0, 0, 0, h,
                new int[]{COLOR_TOP, COLOR_MID1, COLOR_MID2, COLOR_BOTTOM},
                new float[]{0f, 0.35f, 0.70f, 1f}, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, bgPaint);

        float r;
        r = Math.min(w, h) * 0.70f;
        blob(canvas, w * 0.14f, h * 0.06f, r, BLOB_BLUE,   0.28f);
        r = Math.min(w, h) * 0.65f;
        blob(canvas, w * 0.84f, h * 0.12f, r, BLOB_PURPLE, 0.24f);
        r = Math.min(w, h) * 0.75f;
        blob(canvas, w * 0.50f, h * 0.50f, r, BLOB_CYAN,   0.16f);
        r = Math.min(w, h) * 0.95f;
        blob(canvas, w * 0.50f, h * 1.00f, r, BLOB_BLACK,  0.28f);
    }

    private void blob(Canvas c, float cx, float cy, float r, int color, float a) {
        blobPaint.setShader(new RadialGradient(cx, cy, r,
                argb(color, a), argb(color, 0f), Shader.TileMode.CLAMP));
        c.drawCircle(cx, cy, r, blobPaint);
    }

    private static int argb(int color, float a) {
        return ((int)(255*a)<<24) | (color&0x00FFFFFF);
    }
}
