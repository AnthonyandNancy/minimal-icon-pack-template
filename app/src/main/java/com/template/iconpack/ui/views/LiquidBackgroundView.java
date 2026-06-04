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
 * Dark-toned background with 4-layer gradient + 4 soft blobs.
 * Base: #8EA5C6 → #7398A8  (muted blue-grey → teal)
 */
public class LiquidBackgroundView extends View {

    private final Paint bgPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint blobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final int TOP_COLOR     = 0xFF8EA5C6;
    private static final int MID1_COLOR    = 0xFFA99FC9;
    private static final int MID2_COLOR    = 0xFF8CBCC0;
    private static final int BOTTOM_COLOR  = 0xFF7398A8;

    private static final int BLOB_BLUE   = 0xFF5EA8FF;
    private static final int BLOB_PURPLE = 0xFF8E6CFF;
    private static final int BLOB_CYAN   = 0xFF3ED8C8;
    private static final int BLOB_DARK   = 0xFF2D4B5D;

    public LiquidBackgroundView(Context c) { super(c); }
    public LiquidBackgroundView(Context c, AttributeSet a) { super(c, a); }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth(), h = getHeight();
        if (w == 0 || h == 0) return;

        // 4-stop vertical gradient
        bgPaint.setShader(new LinearGradient(0, 0, 0, h,
                new int[]{TOP_COLOR, MID1_COLOR, MID2_COLOR, BOTTOM_COLOR},
                new float[]{0f, 0.35f, 0.70f, 1f},
                Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, bgPaint);

        float r;

        r = Math.min(w, h) * 0.62f;
        blob(canvas, w * 0.16f, h * 0.08f, r, BLOB_BLUE, 0.26f);

        r = Math.min(w, h) * 0.58f;
        blob(canvas, w * 0.86f, h * 0.14f, r, BLOB_PURPLE, 0.24f);

        r = Math.min(w, h) * 0.72f;
        blob(canvas, w * 0.48f, h * 0.52f, r, BLOB_CYAN, 0.18f);

        r = Math.min(w, h) * 0.90f;
        blob(canvas, w * 0.5f, h * 1.02f, r, BLOB_DARK, 0.16f);
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
