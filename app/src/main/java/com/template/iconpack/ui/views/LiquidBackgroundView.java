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
 * Airy mist-blue gradient background: #EAF2FF → #F5F8FF.
 * Soft light blobs — blue, purple, cyan, warm white.
 */
public class LiquidBackgroundView extends View {

    private final Paint bgPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint blobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final int[] COLORS = {
        0xFFEAF2FF, 0xFFDDE8F8, 0xFFE8E2F5, 0xFFD7EEF0, 0xFFF5F8FF
    };
    private static final float[] STOPS = {0f, 0.25f, 0.50f, 0.75f, 1f};

    private static final int BLUE   = 0xFF8DBBFF;
    private static final int PURPLE = 0xFFB9A2FF;
    private static final int CYAN   = 0xFF9DEFE7;
    private static final int WHITE  = 0xFFFFFFFF;
    private static final int VIGN   = 0xFFB8C7D9;

    public LiquidBackgroundView(Context c) { super(c); }
    public LiquidBackgroundView(Context c, AttributeSet a) { super(c, a); }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth(), h = getHeight();
        if (w == 0 || h == 0) return;

        bgPaint.setShader(new LinearGradient(0, 0, 0, h, COLORS, STOPS, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, bgPaint);

        float r;
        r = Math.min(w, h) * 0.70f;
        blob(canvas, w*0.12f, h*0.08f, r, BLUE,   0.26f);
        r = Math.min(w, h) * 0.62f;
        blob(canvas, w*0.88f, h*0.18f, r, PURPLE, 0.22f);
        r = Math.min(w, h) * 0.76f;
        blob(canvas, w*0.45f, h*0.58f, r, CYAN,   0.20f);
        r = Math.min(w, h) * 0.95f;
        blob(canvas, w*0.50f, h*1.02f, r, WHITE,  0.28f);
        r = Math.min(w, h) * 1.10f;
        blob(canvas, w*0.50f, h*1.05f, r, VIGN,   0.10f);
    }

    private void blob(Canvas c, float x, float y, float r, int col, float a) {
        blobPaint.setShader(new RadialGradient(x, y, r,
                argb(col, a), argb(col, 0f), Shader.TileMode.CLAMP));
        c.drawCircle(x, y, r, blobPaint);
    }
    private static int argb(int c, float a) { return ((int)(255*a)<<24)|(c&0x00FFFFFF); }
}
