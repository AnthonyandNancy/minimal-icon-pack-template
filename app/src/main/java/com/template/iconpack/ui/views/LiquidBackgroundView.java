package com.template.iconpack.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import com.template.iconpack.ui.glass.LiquidAuroraBackgroundRenderer;

/** iOS 26 rainbow wallpaper — delegates to LiquidAuroraBackgroundRenderer. */
public class LiquidBackgroundView extends View {

    private final LiquidAuroraBackgroundRenderer renderer = new LiquidAuroraBackgroundRenderer();

    public LiquidBackgroundView(Context c) { super(c); }
    public LiquidBackgroundView(Context c, AttributeSet a) { super(c, a); }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        renderer.draw(canvas, getWidth(), getHeight());
    }
}
