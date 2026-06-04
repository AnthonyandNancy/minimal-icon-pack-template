package com.template.iconpack.ui;

import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.view.View;

/**
 * Safe RenderEffect blur for API 31+. No-op on older versions.
 */
public final class GlassBlur {

    private GlassBlur() {}

    /** Apply blur to a View. Safe on all API levels. */
    public static void apply(View view, float radius) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && view != null) {
            view.setRenderEffect(RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP));
        }
    }

    /** Remove blur. */
    public static void clear(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && view != null) {
            view.setRenderEffect(null);
        }
    }

    // ── Presets ───────────────────────────────────────────
    public static void toolbar(View v)    { apply(v, 10f); }
    public static void hero(View v)       { apply(v, 8f); }
    public static void drawer(View v)     { apply(v, 12f); }
    public static void bottomBar(View v)  { apply(v, 14f); }
}
