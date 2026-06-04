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
 * Rich deep gradient — not dead black.
 * #0F172A → #1A1635 → #172A34 → #0D1A1A
 */
public class LiquidBackgroundView extends View {

    private final Paint bgPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint blobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final int COL_TOP    = 0xFF0F172A;  // deep navy
    private static final int COL_MID1   = 0xFF1A1635;  // deep purple-black
    private static final int COL_MID2   = 0xFF172A34;  // dark teal
    private static final int COL_BOTTOM = 0xFF0D1A1A;  // deep cyan-black

    private static final int BLOB_BLUE   = 0xFF2F7DFF;
    private static final int BLOB_PURPLE = 0xFF7C4DFF;
    private static final int BLOB_CYAN   = 0xFF1DD7C2;
    private static final int BLOB_GLOW   = 0xFF152A40;

    public LiquidBackgroundView(Context c) { super(c); }
    public LiquidBackgroundView(Context c, AttributeSet a) { super(c, a); }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth(), h = getHeight();
        if (w == 0 || h == 0) return;

        // 4-stop gradient
        bgPaint.setShader(new LinearGradient(0, 0, 0, h,
                new int[]{COL_TOP, COL_MID1, COL_MID2, COL_BOTTOM},
                new float[]{0f, 0.35f, 0.70f, 1f}, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, bgPaint);

        float r;

        // Large soft-blue bloom top-left
        r = Math.min(w, h) * 0.75f;
        blob(canvas, w * 0.14f, h * 0.10f, r, BLOB_BLUE, 0.30f);

        // Warm purple top-right
        r = Math.min(w, h) * 0.68f;
        blob(canvas, w * 0.86f, h * 0.14f, r, BLOB_PURPLE, 0.26f);

        // Bright cyan centre
        r = Math.min(w, h) * 0.78f;
        blob(canvas, w * 0.48f, h * 0.52f, r, BLOB_CYAN, 0.18f);

        // Subtle deep glow bottom
        r = Math.min(w, h) * 0.88f;
        blob(canvas, w * 0.52f, h * 0.88f, r, BLOB_GLOW, 0.22f);
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
