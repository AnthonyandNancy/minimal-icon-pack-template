package com.template.iconpack.ui.glass;

/**
 * Glass presets tuned for the 9-layer LiquidGlassDrawable.
 * Inspired by liquid-glass-react: layered transparency + edge highlights + saturation.
 */
public final class GlassMaterialFactory {

    private GlassMaterialFactory() {}

    // ── Core modes ──────────────────────────────

    public static GlassMaterialConfig clear(float r) {
        return new GlassMaterialConfig().mode(0).radius(r).elevation(3f)
                .base(0xFFF8FAFC).stroke(0x60FFFFFF)
                .opacity(0.10f).highlight(0.20f).edge(0.28f).shadow(0.18f)
                .noise(0.008f).elasticity(0.18f);
    }

    public static GlassMaterialConfig regular(float r) {
        return new GlassMaterialConfig().mode(1).radius(r).elevation(6f)
                .base(0xFFF1F5F9).stroke(0x90FFFFFF)
                .opacity(0.16f).highlight(0.34f).edge(0.46f).shadow(0.24f)
                .noise(0.012f).elasticity(0.24f);
    }

    public static GlassMaterialConfig prominent(float r) {
        return new GlassMaterialConfig().mode(2).radius(r).elevation(12f)
                .base(0xFFE2E8F0).stroke(0xCCFFFFFF)
                .opacity(0.24f).highlight(0.52f).edge(0.72f).shadow(0.30f)
                .noise(0.016f).elasticity(0.32f).aberration(true);
    }

    // ── Named presets ───────────────────────────

    public static GlassMaterialConfig toolbar()  { return prominent(38f); }
    public static GlassMaterialConfig hero()     { return prominent(34f); }
    public static GlassMaterialConfig drawer()   { return prominent(36f).shadow(0.34f); }
    public static GlassMaterialConfig bottomBar(){ return prominent(36f).opacity(0.28f); }

    public static GlassMaterialConfig statCard()   { return regular(28f); }
    public static GlassMaterialConfig featureCard(){ return regular(26f); }
    public static GlassMaterialConfig aboutCard()  { return regular(24f); }

    public static GlassMaterialConfig listItem()   { return clear(22f); }
    public static GlassMaterialConfig chip()       { return clear(999f); }
    public static GlassMaterialConfig button()     { return clear(22f); }
}
