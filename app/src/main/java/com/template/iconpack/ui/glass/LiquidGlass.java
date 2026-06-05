package com.template.iconpack.ui.glass;

import android.content.Context;

/** Convenience factory for LiquidGlassLayout. */
public final class LiquidGlass {
    private LiquidGlass() {}
    public static LiquidGlassLayout create(Context ctx) { return new LiquidGlassLayout(ctx); }
}
