package com.template.iconpack.ui.glass;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;

/**
 * Renders the fixed Deep Aurora background.
 * Call draw() to render the full background, or drawRegion() for a cropped region.
 */
public class LiquidAuroraBackgroundRenderer {

    private final Paint bgPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint blobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Deep space gradient
    private static final int[]   GRADIENT_COLORS = {0xFF0B1020, 0xFF0E1835, 0xFF111E46, 0xFF172554};
    private static final float[] GRADIENT_STOPS  = {0f, 0.35f, 0.70f, 1f};

    // Large blurred colour blobs
    private static final int[]   BLOB_COLORS = {0xFF3B82F6, 0xFF8B5CF6, 0xFFEC4899, 0xFF06B6D4, 0xFFF97316};
    private static final float[] BLOB_ALPHAS = {0.30f, 0.26f, 0.22f, 0.20f, 0.18f};
    private static final float[] BLOB_SIZES  = {0.70f, 0.65f, 0.72f, 0.58f, 0.62f};
    private static final float[] BLOB_X      = {0.15f, 0.80f, 0.50f, 0.25f, 0.75f};
    private static final float[] BLOB_Y      = {0.15f, 0.25f, 0.60f, 0.78f, 0.85f};

    private float animT;

    public void setAnimTime(float t) { this.animT = t; }

    /** Draw full background at (0,0) → (width, height). */
    public void draw(Canvas canvas, int width, int height) {
        drawRegion(canvas, 0, 0, width, height, width, height);
    }

    /**
     * Draw background region at canvas (0,0), sampled from [sx, sy] in the global bg.
     * @param canvas   target canvas (already translated to correct offset)
     * @param sx       sample start x in global background
     * @param sy       sample start y in global background
     * @param sw       sample width
     * @param sh       sample height
     * @param rootW    full background width
     * @param rootH    full background height
     */
    public void drawRegion(Canvas canvas,
                           int sx, int sy, int sw, int sh,
                           int rootW, int rootH) {
        // Base gradient (fills the full root area, then clip to region)
        bgPaint.setShader(new LinearGradient(0, 0, 0, rootH, GRADIENT_COLORS, GRADIENT_STOPS, Shader.TileMode.CLAMP));
        canvas.drawRect(sx, sy, sx + sw, sy + sh, bgPaint);

        // Draw blobs that intersect with the sampled region
        float s = Math.min(rootW, rootH);
        for (int i = 0; i < BLOB_COLORS.length; i++) {
            float cx = rootW * BLOB_X[i];
            float cy = rootH * BLOB_Y[i];
            float r  = s * BLOB_SIZES[i];

            // Only draw if blob overlaps the region
            if (sx + sw >= cx - r && sx <= cx + r && sy + sh >= cy - r && sy <= cy + r) {
                blobPaint.setShader(new RadialGradient(cx, cy, r,
                        argb(BLOB_COLORS[i], BLOB_ALPHAS[i]),
                        argb(BLOB_COLORS[i], 0f),
                        Shader.TileMode.CLAMP));
                canvas.drawRect(sx, sy, sx + sw, sy + sh, blobPaint);
            }
        }
    }

    private static int argb(int color, float alpha) {
        return ((int)(255 * alpha) << 24) | (color & 0x00FFFFFF);
    }
}
