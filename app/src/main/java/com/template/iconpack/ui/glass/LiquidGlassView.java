package com.template.iconpack.ui.glass;

import android.content.Context;
import android.graphics.Outline;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;

/**
 * Reusable glass FrameLayout. Configure with GlassMaterialConfig.
 * Automatically applies LiquidGlassDrawable background, outline, elevation, optional blur.
 */
public class LiquidGlassView extends FrameLayout {

    private GlassMaterialConfig glassConfig;
    private LiquidGlassDrawable glassDrawable;
    private float density;

    public LiquidGlassView(Context context) {
        this(context, null);
    }

    public LiquidGlassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = context.getResources().getDisplayMetrics().density;
        setConfig(GlassMaterialFactory.regular(26f));
    }

    public void setConfig(GlassMaterialConfig config) {
        this.glassConfig = config;
        this.glassDrawable = new LiquidGlassDrawable(config, density);
        setBackground(glassDrawable);
        setElevation(config.elevationDp * density);
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                float r = config.cornerRadiusDp * density;
                if (config.cornerRadii != null) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(),
                            config.cornerRadii[0]);
                } else {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), r);
                }
            }
        });
        setClipToOutline(true);

        // Optional RenderEffect blur
        if (config.enableRenderEffectBlur && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            float blurR = config.blurAmount * 24f;
            setRenderEffect(RenderEffect.createBlurEffect(blurR, blurR, Shader.TileMode.CLAMP));
        }

        // Re-attach motion if needed
        GlassMotion.attach(this, config.elasticity);
    }

    public void setGlassMode(int mode) {
        switch (mode) {
            case GlassMaterialConfig.MODE_CLEAR:
                setConfig(GlassMaterialFactory.clear(glassConfig.cornerRadiusDp)); break;
            case GlassMaterialConfig.MODE_REGULAR:
                setConfig(GlassMaterialFactory.regular(glassConfig.cornerRadiusDp)); break;
            case GlassMaterialConfig.MODE_PROMINENT:
                setConfig(GlassMaterialFactory.prominent(glassConfig.cornerRadiusDp)); break;
        }
    }

    public GlassMaterialConfig getGlassConfig() { return glassConfig; }
    public LiquidGlassDrawable getGlassDrawable() { return glassDrawable; }
}
