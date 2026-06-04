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
 * Deep-space background: #050A12 → #0B1525 → #101E2E → #0C2A30 → #02060B
 * + 4 blobs.
 */
public class LiquidBackgroundView extends View {

    private final Paint bgPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint blobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final int[] COLORS = {
        0xFF050A12, 0xFF0B1525, 0xFF101E2E, 0xFF0C2A30, 0xFF02060B
    };
    private static final float[] STOPS = {0f, 0.25f, 0.50f, 0.75f, 1f};

    private static final int BLUE   = 0xFF2F7DFF;
    private static final int PURPLE = 0xFF7C4DFF;
    private static final int CYAN   = 0xFF16D6C8;
    private static final int BLACK  = 0xFF000000;

    public LiquidBackgroundView(Context c) { super(c); }
    public LiquidBackgroundView(Context c, AttributeSet a) { super(c, a); }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth(), h = getHeight();
        if (w == 0 || h == 0) return;

        bgPaint.setShader(new LinearGradient(0, 0, 0, h, COLORS, STOPS, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, bgPaint);

        float r;
        r = Math.min(w, h) * 0.72f;
        blob(canvas, w * 0.12f, h * 0.10f, r, BLUE,   0.22f);
        r = Math.min(w, h) * 0.68f;
        blob(canvas, w * 0.92f, h * 0.14f, r, PURPLE, 0.20f);
        r = Math.min(w, h) * 0.78f;
        blob(canvas, w * 0.48f, h * 0.58f, r, CYAN,   0.14f);
        r = Math.min(w, h) * 1.05f;
        blob(canvas, w * 0.50f, h * 1.06f, r, BLACK,  0.35f);
    }

    private void blob(Canvas c, float x, float y, float r, int col, float a) {
        blobPaint.setShader(new RadialGradient(x, y, r,
                argb(col, a), argb(col, 0f), Shader.TileMode.CLAMP));
        c.drawCircle(x, y, r, blobPaint);
    }
    private static int argb(int c, float a) { return ((int)(255*a)<<24)|(c&0x00FFFFFF); }
}
