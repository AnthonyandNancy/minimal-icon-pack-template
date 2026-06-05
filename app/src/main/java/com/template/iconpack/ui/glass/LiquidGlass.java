package com.template.iconpack.ui.glass;

import android.content.Context;

/**
 * One-line LiquidGlassView factory.
 * Usage: LiquidGlass.prominent(ctx), LiquidGlass.regular(ctx), etc.
 */
public final class LiquidGlass {

    private LiquidGlass() {}

    public static LiquidGlassView prominent(Context ctx) {
        LiquidGlassView v = new LiquidGlassView(ctx);
        v.setConfig(GlassMaterialFactory.hero());
        return v;
    }

    public static LiquidGlassView regular(Context ctx) {
        LiquidGlassView v = new LiquidGlassView(ctx);
        v.setConfig(GlassMaterialFactory.statCard());
        return v;
    }

    public static LiquidGlassView clear(Context ctx) {
        LiquidGlassView v = new LiquidGlassView(ctx);
        v.setConfig(GlassMaterialFactory.listItem());
        return v;
    }

    public static LiquidGlassView toolbar(Context ctx) {
        LiquidGlassView v = new LiquidGlassView(ctx);
        v.setConfig(GlassMaterialFactory.toolbar());
        return v;
    }

    public static LiquidGlassView hero(Context ctx) {
        LiquidGlassView v = new LiquidGlassView(ctx);
        v.setConfig(GlassMaterialFactory.hero());
        return v;
    }

    public static LiquidGlassView statCard(Context ctx) {
        LiquidGlassView v = new LiquidGlassView(ctx);
        v.setConfig(GlassMaterialFactory.statCard());
        return v;
    }

    public static LiquidGlassView bottomBar(Context ctx) {
        LiquidGlassView v = new LiquidGlassView(ctx);
        v.setConfig(GlassMaterialFactory.bottomActionBar());
        return v;
    }

    public static LiquidGlassView drawer(Context ctx) {
        LiquidGlassView v = new LiquidGlassView(ctx);
        v.setConfig(GlassMaterialFactory.drawer());
        return v;
    }

    public static LiquidGlassView listItem(Context ctx) {
        LiquidGlassView v = new LiquidGlassView(ctx);
        v.setConfig(GlassMaterialFactory.listItem());
        return v;
    }

    public static LiquidGlassView chip(Context ctx) {
        LiquidGlassView v = new LiquidGlassView(ctx);
        v.setConfig(GlassMaterialFactory.chip());
        return v;
    }
}
