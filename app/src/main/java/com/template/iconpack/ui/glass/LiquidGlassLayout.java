package com.template.iconpack.ui.glass;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.Random;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

/**
 * Liquid glass container: BlurView (real-time background blur) + GlassOverlay (highlights, edges).
 *
 * Usage:
 *   <com.template.iconpack.ui.glass.LiquidGlassLayout
 *       app:cornerRadius="28dp" ...>
 *       <!-- children rendered sharp on top -->
 *   </com.template.iconpack.ui.glass.LiquidGlassLayout>
 *
 * Requires a BlurView-compatible root (blurView.setupWith(rootViewGroup).setBlurAlgorithm(...)).
 * Call bindBlur(FrameLayout root) after inflating.
 */
public class LiquidGlassLayout extends FrameLayout {

    private final BlurView blurView;
    private final GlassOverlay overlay;
    private final Paint overlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private float cornerRadiusPx;
    private float d;

    public LiquidGlassLayout(Context context) { this(context, null); }
    public LiquidGlassLayout(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public LiquidGlassLayout(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); init(); }

    private void init() {
        d = getContext().getResources().getDisplayMetrics().density;
        cornerRadiusPx = 28f * d;
        setClipToOutline(true);
        setOutlineProvider((view, outline) -> {
            outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cornerRadiusPx);
        });

        // BlurView fills entire parent
        blurView = new BlurView(getContext());
        blurView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(blurView, 0);

        // Glass overlay on top of blur
        overlay = new GlassOverlay(getContext());
        overlay.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(overlay);
    }

    /** Call after setContentView to bind blur to the root layout. */
    public void bindBlur(FrameLayout root) {
        blurView.setupWith(root, new RenderScriptBlur(getContext()))
                .setFrameClearDrawable(null)
                .setBlurRadius(18f)
                .setOverlayColor(0x00000000)
                .setBlurAutoUpdate(true);
    }

    public void setBlurRadius(float r) { blurView.setBlurRadius(r); }
    public void setCornerRadiusDp(float dp) {
        cornerRadiusPx = dp * d;
        setOutlineProvider((view, outline) -> {
            outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cornerRadiusPx);
        });
    }

    @Override protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        rect.set(0, 0, w, h);
        overlay.cornerRadiusPx = cornerRadiusPx;
    }

    /** Overlay that draws glass highlights, edges, chromatic aberration on top of BlurView. */
    private static class GlassOverlay extends View {
        float cornerRadiusPx;
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF r = new RectF();
        private final Random rng = new Random(42L);

        GlassOverlay(Context c) { super(c); }

        @Override protected void onSizeChanged(int w, int h, int ow, int oh) {
            super.onSizeChanged(w, h, ow, oh);
            r.set(0, 0, w, h);
        }

        @Override protected void onDraw(Canvas canvas) {
            if (cornerRadiusPx <= 0) return;
            float rr = cornerRadiusPx, d = getResources().getDisplayMetrics().density;

            Path clip = new Path(); clip.addRoundRect(r, rr, rr, Path.Direction.CW);
            int save = canvas.save();
            canvas.clipPath(clip);

            // Tint
            p.reset(); p.setColor(0x88FFFFFF); p.setAlpha(100);
            canvas.drawRoundRect(r, rr, rr, p);

            // Saturation
            p.reset(); p.setAlpha(40);
            p.setShader(new LinearGradient(r.left, r.top, r.right, r.bottom,
                    new int[]{0x4D96FF, 0xA78BFA, 0x22D3C5}, new float[]{0,0.5f,1f}, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(r, rr, rr, p);

            // Inner glow
            p.reset(); p.setShader(new RadialGradient(r.centerX(), r.centerY(), Math.min(r.width(),r.height())*0.5f,
                    0x30FFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(r, rr, rr, p);

            // Highlight
            p.reset(); p.setShader(new LinearGradient(r.left, r.top, r.right, r.bottom,
                    0x80FFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(r, rr, rr, p);

            // Edge refraction
            p.reset(); p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(1.2f * d);
            p.setColor(0x80FFFFFF);
            canvas.drawRoundRect(r, rr, rr, p);

            // Chromatic aberration
            p.setStrokeWidth(0.45f * d);
            p.setColor(0x30FF6666);
            RectF redR = new RectF(r.left-d*0.5f, r.top-d*0.5f, r.right-d*0.5f, r.bottom-d*0.5f);
            canvas.drawRoundRect(redR, rr, rr, p);
            p.setColor(0x206666FF);
            RectF blueR = new RectF(r.left+d*0.5f, r.top+d*0.5f, r.right+d*0.5f, r.bottom+d*0.5f);
            canvas.drawRoundRect(blueR, rr, rr, p);

            // Bottom shadow
            p.reset(); p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(r.left, r.bottom-rr*1.5f, r.left, r.bottom+1,
                    0x00000000, 0x18000000, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(r, rr, rr, p);

            // Noise
            p.reset(); p.setColor(0x04FFFFFF);
            int step = (int)(rr*0.12f);
            Random rng = new Random(42L);
            for (float x = r.left+step; x < r.right; x+=step)
                for (float y = r.top+step; y < r.bottom; y+=step)
                    if (rng.nextFloat() > 0.5f)
                        canvas.drawPoint(x+rng.nextFloat()*step, y+rng.nextFloat()*step, p);

            canvas.restoreToCount(save);

            // Border rims
            p.reset(); p.setStyle(Paint.Style.STROKE);
            Path bp = new Path(); bp.addRoundRect(r, rr, rr, Path.Direction.CW);

            p.setStrokeWidth(d * 1.0f);
            p.setColor(0x60FFFFFF);
            p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
            canvas.drawPath(bp, p);

            p.setXfermode(null);
            p.setStrokeWidth(d * 0.5f);
            p.setColor(0x40FFFFFF);
            canvas.drawPath(bp, p);

            p.setStrokeWidth(1.2f * d);
            p.setColor(0x40FFFFFF);
            canvas.drawPath(bp, p);

            // Top hairline
            p.reset(); p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(0.25f * d);
            p.setColor(0x80FFFFFF);
            float ty = r.top + 0.5f*d;
            canvas.drawLine(r.left+rr*0.4f, ty, r.right-rr*0.4f, ty, p);
        }
    }
}
