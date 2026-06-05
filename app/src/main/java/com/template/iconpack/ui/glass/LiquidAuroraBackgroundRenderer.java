package com.template.iconpack.ui.glass;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;

/**
 * iOS 26 rainbow wallpaper renderer.
 * Warm base gradient + 6 drifting colour blobs.
 */
public class LiquidAuroraBackgroundRenderer {

    private final Paint bgPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint blobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final int[]   GC = {0xFFF2EBE5, 0xFFEBE0F0, 0xFFE5E8F5, 0xFFE8F0F5, 0xFFF0EBE5};
    private static final float[] GS = {0f, 0.3f, 0.55f, 0.78f, 1f};

    private static final int[]   BC = {
        0xFFA8D8EA, 0xFFC9B8E8, 0xFFF5C4B8, 0xFFB8E8D0, 0xFFF5E4B8, 0xFFF5C4D8
    };
    private static final float[] BA = {0.38f, 0.32f, 0.34f, 0.28f, 0.30f, 0.26f};
    private static final float[] BS = {0.48f, 0.52f, 0.44f, 0.50f, 0.46f, 0.42f};
    private static final float[] BX = {0.12f, 0.82f, 0.48f, 0.22f, 0.72f, 0.58f};
    private static final float[] BY = {0.18f, 0.32f, 0.58f, 0.78f, 0.48f, 0.82f};

    public void setAnimTime(float t) {} // reserved for animation

    public void draw(Canvas canvas, int width, int height) {
        drawRegion(canvas, 0, 0, width, height, width, height);
    }

    public void drawRegion(Canvas canvas, int sx, int sy, int sw, int sh, int rootW, int rootH) {
        bgPaint.setShader(new LinearGradient(0, 0, rootW, rootH, GC, GS, Shader.TileMode.CLAMP));
        canvas.drawRect(sx, sy, sx + sw, sy + sh, bgPaint);

        float s = Math.min(rootW, rootH);
        for (int i = 0; i < 6; i++) {
            float cx = rootW * BX[i], cy = rootH * BY[i], r = s * BS[i];
            if (sx + sw >= cx - r && sx <= cx + r && sy + sh >= cy - r && sy <= cy + r) {
                blobPaint.setShader(new RadialGradient(cx, cy, r,
                        argb(BC[i], BA[i]), argb(BC[i], 0f), Shader.TileMode.CLAMP));
                canvas.drawRect(sx, sy, sx + sw, sy + sh, blobPaint);
            }
        }
    }

    private static int argb(int c, float a) { return ((int)(255*a)<<24)|(c&0x00FFFFFF); }
}
