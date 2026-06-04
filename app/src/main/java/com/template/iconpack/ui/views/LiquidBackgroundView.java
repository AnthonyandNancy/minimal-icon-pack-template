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
 * Deep-space 5-stop gradient background:
 * #09111D → #101B2D → #132233 → #0E2830 → #050B12
 */
public class LiquidBackgroundView extends View {

    private final Paint bgPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint blobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final int[] GRAD_COLORS = {
        0xFF09111D, 0xFF101B2D, 0xFF132233, 0xFF0E2830, 0xFF050B12
    };
    private static final float[] GRAD_STOPS = {0f, 0.25f, 0.50f, 0.75f, 1f};

    private static final int BLOB_BLUE   = 0xFF2F7DFF;
    private static final int BLOB_PURPLE = 0xFF7C4DFF;
    private static final int BLOB_CYAN   = 0xFF15D3C5;
    private static final int BLOB_BLACK  = 0xFF000000;

    public LiquidBackgroundView(Context c) { super(c); }
    public LiquidBackgroundView(Context c, AttributeSet a) { super(c, a); }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth(), h = getHeight();
        if (w == 0 || h == 0) return;

        bgPaint.setShader(new LinearGradient(0, 0, 0, h,
                GRAD_COLORS, GRAD_STOPS, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, bgPaint);

        float r;
        r = Math.min(w, h) * 0.68f;
        blob(canvas, w * 0.14f, h * 0.08f, r, BLOB_BLUE, 0.26f);
        r = Math.min(w, h) * 0.62f;
        blob(canvas, w * 0.86f, h * 0.14f, r, BLOB_PURPLE, 0.22f);
        r = Math.min(w, h) * 0.75f;
        blob(canvas, w * 0.48f, h * 0.50f, r, BLOB_CYAN, 0.14f);
        r = Math.min(w, h) * 0.95f;
        blob(canvas, w * 0.50f, h * 0.94f, r, BLOB_BLACK, 0.32f);
    }

    private void blob(Canvas c, float cx, float cy, float r, int color, float a) {
        blobPaint.setShader(new RadialGradient(cx, cy, r,
                argb(color, a), argb(color, 0f), Shader.TileMode.CLAMP));
        c.drawCircle(cx, cy, r, blobPaint);
    }
    private static int argb(int c, float a) { return ((int)(255*a)<<24) | (c&0x00FFFFFF); }
}
