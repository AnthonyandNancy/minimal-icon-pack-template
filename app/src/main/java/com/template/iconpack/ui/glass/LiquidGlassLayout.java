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
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;

import java.util.Random;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderEffectBlur;
import eightbitlab.com.blurview.RenderScriptBlur;

public class LiquidGlassLayout extends FrameLayout {

    private BlurView blurView;
    private GlassOverlay overlay;
    private float cornerRadiusPx;
    private float d;
    private boolean blurReady;

    public LiquidGlassLayout(Context context) { super(context); init(); }
    public LiquidGlassLayout(Context context, AttributeSet attrs) { super(context, attrs); init(); }

    private void init() {
        d = getContext().getResources().getDisplayMetrics().density;
        cornerRadiusPx = 28f * d;
        setClipToOutline(true);
        setOutlineProvider(new ViewOutlineProvider() {
            @Override public void getOutline(View v, Outline o) {
                o.setRoundRect(0, 0, v.getWidth(), v.getHeight(), cornerRadiusPx);
            }
        });

        blurView = new BlurView(getContext());
        blurView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(blurView, 0);

        overlay = new GlassOverlay(getContext());
        overlay.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(overlay);
    }

    public void bindBlur(FrameLayout root) {
        if (root == null || blurReady) return;
        try {
            blurView.setupWith(root)
                    .setBlurRadius(14f)
                    .setOverlayColor(0x00000000)
                    .setBlurAutoUpdate(true);
            blurReady = true;
        } catch (Exception e) {
            blurView.setVisibility(GONE);
        }
    }

    @Override protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        overlay.cornerRadiusPx = cornerRadiusPx;
        overlay.rect.set(0, 0, w, h);
    }

    static class GlassOverlay extends View {
        float cornerRadiusPx;
        final RectF rect = new RectF();
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        GlassOverlay(Context c) { super(c); }

        @Override protected void onDraw(Canvas canvas) {
            if (cornerRadiusPx <= 0 || rect.isEmpty()) return;
            float rr = cornerRadiusPx, d = getResources().getDisplayMetrics().density;
            Path clip = new Path(); clip.addRoundRect(rect, rr, rr, Path.Direction.CW);
            int save = canvas.save();
            canvas.clipPath(clip);

            p.reset(); p.setColor(0x88FFFFFF); p.setAlpha(100);
            canvas.drawRoundRect(rect, rr, rr, p);

            p.reset(); p.setAlpha(40);
            p.setShader(new LinearGradient(rect.left, rect.top, rect.right, rect.bottom,
                    new int[]{0x4D96FF, 0xA78BFA, 0x22D3C5}, new float[]{0,0.5f,1f}, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(rect, rr, rr, p);

            p.reset(); p.setShader(new RadialGradient(rect.centerX(), rect.centerY(),
                    Math.min(rect.width(),rect.height())*0.5f, 0x30FFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(rect, rr, rr, p);

            p.reset(); p.setShader(new LinearGradient(rect.left, rect.top, rect.right, rect.bottom,
                    0x80FFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(rect, rr, rr, p);

            p.reset(); p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(1.2f*d); p.setColor(0x80FFFFFF);
            canvas.drawRoundRect(rect, rr, rr, p);

            p.setStrokeWidth(0.45f*d); p.setColor(0x30FF6666);
            canvas.drawRoundRect(new RectF(rect.left-d*0.5f,rect.top-d*0.5f,rect.right-d*0.5f,rect.bottom-d*0.5f), rr, rr, p);
            p.setColor(0x206666FF);
            canvas.drawRoundRect(new RectF(rect.left+d*0.5f,rect.top+d*0.5f,rect.right+d*0.5f,rect.bottom+d*0.5f), rr, rr, p);

            p.reset(); p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(rect.left, rect.bottom-rr*1.5f, rect.left, rect.bottom+1,
                    0x00000000, 0x18000000, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(rect, rr, rr, p);

            p.reset(); p.setColor(0x04FFFFFF);
            int step = (int)(rr*0.12f); Random rng = new Random(42L);
            for (float x=rect.left+step; x<rect.right; x+=step)
                for (float y=rect.top+step; y<rect.bottom; y+=step)
                    if (rng.nextFloat()>0.5f) canvas.drawPoint(x+rng.nextFloat()*step, y+rng.nextFloat()*step, p);

            canvas.restoreToCount(save);

            p.reset(); p.setStyle(Paint.Style.STROKE);
            Path bp = new Path(); bp.addRoundRect(rect, rr, rr, Path.Direction.CW);
            p.setStrokeWidth(d*1.0f); p.setColor(0x60FFFFFF);
            p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
            canvas.drawPath(bp, p);
            p.setXfermode(null);
            p.setStrokeWidth(d*0.5f); p.setColor(0x40FFFFFF); canvas.drawPath(bp, p);
            p.setStrokeWidth(1.2f*d); p.setColor(0x40FFFFFF); canvas.drawPath(bp, p);

            p.reset(); p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(0.25f*d); p.setColor(0x80FFFFFF);
            float ty = rect.top+0.5f*d;
            canvas.drawLine(rect.left+rr*0.4f, ty, rect.right-rr*0.4f, ty, p);
        }
    }
}
