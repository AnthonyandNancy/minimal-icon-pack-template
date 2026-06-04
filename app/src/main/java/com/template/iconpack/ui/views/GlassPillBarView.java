package com.template.iconpack.ui.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.template.iconpack.ui.LiquidGlassDrawable;

/**
 * Pill-shaped glass bar — like the music player mini player / bottom tab bar.
 * Renders as a large-radius glass capsule. Children are laid out horizontally.
 */
public class GlassPillBarView extends FrameLayout {

    private final LinearLayout contentRow;

    public GlassPillBarView(Context context) {
        this(context, null);
    }

    public GlassPillBarView(Context context, AttributeSet attrs) {
        super(context, attrs);

        float density = context.getResources().getDisplayMetrics().density;
        setBackground(LiquidGlassDrawable.pillBar(density));
        setElevation(12f * density);
        setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override
            public void getOutline(android.view.View view, android.graphics.Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(),
                        36f * density);
            }
        });
        setClipToOutline(true);

        contentRow = new LinearLayout(context);
        contentRow.setOrientation(LinearLayout.HORIZONTAL);
        contentRow.setGravity(Gravity.CENTER_VERTICAL);
        int pad = (int)(16f * density);
        contentRow.setPadding(pad, 0, pad, 0);

        addView(contentRow, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
    }

    /** Add a selectable pill item */
    public void addPillItem(String label, Drawable icon, Runnable onClick) {
        // Not implemented in detail — placeholder for future use
        // For now, the pill bar acts as a glass container
    }

    /** Quick factory for a centered title-only pill bar (like toolbar) */
    public static GlassPillBarView createToolbar(Context context, String title) {
        GlassPillBarView bar = new GlassPillBarView(context);
        TextView tv = new TextView(context);
        tv.setText(title);
        tv.setTextSize(22);
        tv.setTextColor(0xFF111827);
        tv.setGravity(Gravity.CENTER);
        bar.addView(tv, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        return bar;
    }
}
