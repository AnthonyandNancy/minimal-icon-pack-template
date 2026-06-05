package com.template.iconpack.ui.glass;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;

import java.util.Random;

import eightbitlab.com.blurview.BlurView;

/**
 * Liquid glass using Dimezis BlurView 3.x (no RenderScript).
 * Real-time background blur + glass overlay with highlights.
 */
public class LiquidGlassLayout extends FrameLayout {

    private final BlurView blurView;
    private final GlassOverlay overlay;
    private float cornerRadiusDp = 28f;
    private float d;

    public LiquidGlassLayout(Context context) { this(context, null); }
    public LiquidGlassLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        d = context.getResources().getDisplayMetrics().density;
        setClipToOutline(true);
        int r = (int)(cornerRadiusDp * d);
        setOutlineProvider(new ViewOutlineProvider() {
            @Override public void getOutline(View v, Outline o) {
                o.setRoundRect(0, 0, v.getWidth(), v.getHeight(), r);
            }
        });

        blurView = new BlurView(context);
        blurView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(blurView);

        overlay = new GlassOverlay(context);
        overlay.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(overlay);
    }

    /** Bind to the root ViewGroup containing the background to blur. Call after setContentView. */
    public void setupBlur(View root) {
        blurView.setupWith(root)
                .setBlurRadius(16f)
                .setBlurAutoUpdate(true)
                .setOverlayColor(0x00000000);
    }

    /** Glass overlay: tint / highlights / edges / chromatic aberration. */
    static class GlassOverlay extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();
        private float rr;

        GlassOverlay(Context c) { super(c); }

        @Override protected void onSizeChanged(int w, int h, int ow, int oh) {
            super.onSizeChanged(w, h, ow, oh);
            rect.set(0, 0, w, h);
            rr = 28f * getResources().getDisplayMetrics().density;
        }

        @Override protected void onDraw(Canvas canvas) {
            if (rect.isEmpty() || rr <= 0) return;
            float d = getResources().getDisplayMetrics().density;
            Path clip = new Path(); clip.addRoundRect(rect, rr, rr, Path.Direction.CW);
            int save = canvas.save();
            canvas.clipPath(clip);

            // Tint
            p.reset(); p.setColor(0x88FFFFFF); p.setAlpha(105);
            canvas.drawRoundRect(rect, rr, rr, p);

            // Saturation
            p.reset(); p.setAlpha(45);
            p.setShader(new LinearGradient(rect.left, rect.top, rect.right, rect.bottom,
                    new int[]{0x4D96FF, 0xA78BFA, 0x22D3C5}, new float[]{0,0.5f,1f}, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(rect, rr, rr, p);

            // Inner glow
            p.reset(); p.setShader(new RadialGradient(rect.centerX(), rect.centerY(),
                    Math.min(rect.width(),rect.height())*0.5f, 0x38FFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(rect, rr, rr, p);

            // Directional highlight
            p.reset(); p.setShader(new LinearGradient(rect.left, rect.top, rect.right, rect.bottom,
                    0x80FFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(rect, rr, rr, p);

            // Edge stroke
            p.reset(); p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(1.2f*d); p.setColor(0x80FFFFFF);
            canvas.drawRoundRect(rect, rr, rr, p);

            // Chromatic aberration
            p.setStrokeWidth(0.45f*d); p.setColor(0x30FF6666);
            canvas.drawRoundRect(new RectF(rect.left-d*0.5f,rect.top-d*0.5f,rect.right-d*0.5f,rect.bottom-d*0.5f), rr, rr, p);
            p.setColor(0x206666FF);
            canvas.drawRoundRect(new RectF(rect.left+d*0.5f,rect.top+d*0.5f,rect.right+d*0.5f,rect.bottom+d*0.5f), rr, rr, p);

            // Bottom shadow
            p.reset(); p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(rect.left, rect.bottom-rr*1.5f, rect.left, rect.bottom+1,
                    0x00000000, 0x1A000000, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(rect, rr, rr, p);

            // Noise
            p.reset(); p.setColor(0x04FFFFFF);
            int step = (int)(rr*0.12f); Random rng = new Random(42L);
            for (float x=rect.left+step; x<rect.right; x+=step)
                for (float y=rect.top+step; y<rect.bottom; y+=step)
                    if (rng.nextFloat()>0.5f) canvas.drawPoint(x+rng.nextFloat()*step, y+rng.nextFloat()*step, p);

            canvas.restoreToCount(save);

            // Border rims
            p.reset(); p.setStyle(Paint.Style.STROKE);
            Path bp = new Path(); bp.addRoundRect(rect, rr, rr, Path.Direction.CW);
            p.setStrokeWidth(d*1.0f); p.setColor(0x60FFFFFF);
            p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
            canvas.drawPath(bp, p);
            p.setXfermode(null);
            p.setStrokeWidth(d*0.5f); p.setColor(0x40FFFFFF); canvas.drawPath(bp, p);
            p.setStrokeWidth(1.2f*d); p.setColor(0x40FFFFFF); canvas.drawPath(bp, p);

            // Top hairline
            p.reset(); p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(0.25f*d); p.setColor(0x80FFFFFF);
            float ty = rect.top+0.5f*d;
            canvas.drawLine(rect.left+rr*0.4f, ty, rect.right-rr*0.4f, ty, p);
        }
    }
}
