package com.template.iconpack.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/** Soft gradient header background — warm ivory to lavender. */
public class LiquidBackgroundView extends View {

    private final Paint bgPaint = new Paint();
    private final Paint blobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public LiquidBackgroundView(Context c) { super(c); }
    public LiquidBackgroundView(Context c, AttributeSet a) { super(c, a); }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth(), h = getHeight();
        if (w == 0 || h == 0) return;

        // Soft warm gradient
        bgPaint.setShader(new android.graphics.LinearGradient(0, 0, w, h,
                0xFFF7F4EE, 0xFFF1EEF8, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, bgPaint);

        // Subtle decorative blobs
        float s = Math.min(w, h);
        blob(canvas, w * 0.2f, h * 0.3f, s * 0.5f, 0xFFE8E3FF, 0.25f);
        blob(canvas, w * 0.8f, h * 0.6f, s * 0.45f, 0xFFFFE8E8, 0.20f);
    }

    private void blob(Canvas c, float x, float y, float r, int color, float alpha) {
        blobPaint.setShader(new RadialGradient(x, y, r,
                (color & 0x00FFFFFF) | ((int)(255*alpha) << 24),
                (color & 0x00FFFFFF) | 0, Shader.TileMode.CLAMP));
        c.drawCircle(x, y, r, blobPaint);
    }
}
