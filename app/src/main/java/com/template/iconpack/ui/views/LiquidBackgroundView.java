package com.template.iconpack.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.template.iconpack.R;

/** Soft themed background gradient with subtle decorative blobs. */
public class LiquidBackgroundView extends View {

    private final Paint bgPaint = new Paint();
    private final Paint blobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public LiquidBackgroundView(Context c) { super(c); }
    public LiquidBackgroundView(Context c, AttributeSet a) { super(c, a); }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth(), h = getHeight();
        if (w == 0 || h == 0) return;

        int start = ContextCompat.getColor(getContext(), R.color.bg_mist_start);
        int end = ContextCompat.getColor(getContext(), R.color.bg_mist_end);
        int primaryMist = ContextCompat.getColor(getContext(), R.color.primary_light);
        int warmMist = ContextCompat.getColor(getContext(), R.color.status_unthemed_bg);
        int coolMist = ContextCompat.getColor(getContext(), R.color.icon_bg_blue);

        bgPaint.setShader(new android.graphics.LinearGradient(0, 0, w, h,
                start, end, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, bgPaint);

        float s = Math.min(w, h);
        blob(canvas, w * 0.15f, h * 0.25f, s * 0.55f, primaryMist, 0.30f);
        blob(canvas, w * 0.85f, h * 0.65f, s * 0.45f, warmMist, 0.22f);
        blob(canvas, w * 0.5f, h * 0.4f, s * 0.35f, coolMist, 0.18f);
    }

    private void blob(Canvas c, float x, float y, float r, int color, float alpha) {
        blobPaint.setShader(new RadialGradient(x, y, r,
                (color & 0x00FFFFFF) | ((int) (255 * alpha) << 24), 0x00000000,
                Shader.TileMode.CLAMP));
        c.drawCircle(x, y, r, blobPaint);
    }
}
