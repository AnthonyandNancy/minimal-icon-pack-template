package com.template.iconpack.ui.glass;

/**
 * Deep Aurora dark-mode glass presets.
 * Glass is highly transparent on the dark space background.
 */
public final class GlassMaterialFactory {

    private GlassMaterialFactory() {}

    // ── Core modes (dark bg — lower alpha for high transparency) ──

    public static GlassMaterialConfig clear(float radiusDp) {
        return new GlassMaterialConfig()
                .mode(GlassMaterialConfig.MODE_CLEAR)
                .radius(radiusDp).elevation(3f)
                .base(0x18FFFFFF).stroke(0x44FFFFFF)
                .opacity(0.22f)
                .highlight(0.28f).edge(0.36f).shadow(0.20f)
                .noise(0.010f).elasticity(0.22f);
    }

    public static GlassMaterialConfig regular(float radiusDp) {
        return new GlassMaterialConfig()
                .mode(GlassMaterialConfig.MODE_REGULAR)
                .radius(radiusDp).elevation(6f)
                .base(0x22FFFFFF).stroke(0x60FFFFFF)
                .opacity(0.30f)
                .highlight(0.40f).edge(0.50f).shadow(0.26f)
                .noise(0.014f).elasticity(0.26f);
    }

    public static GlassMaterialConfig prominent(float radiusDp) {
        return new GlassMaterialConfig()
                .mode(GlassMaterialConfig.MODE_PROMINENT)
                .radius(radiusDp).elevation(12f)
                .base(0x30FFFFFF).stroke(0x88FFFFFF)
                .opacity(0.38f)
                .highlight(0.56f).edge(0.72f).shadow(0.32f)
                .noise(0.018f).elasticity(0.34f)
                .aberration(true);
    }

    // ── Named presets ───────────────────────────────────

    public static GlassMaterialConfig toolbar()    { return prominent(38f); }
    public static GlassMaterialConfig hero()       { return prominent(34f); }
    public static GlassMaterialConfig drawer()     { return prominent(36f).opacity(0.35f).shadow(0.30f); }
    public static GlassMaterialConfig bottomBar()  { return prominent(36f).opacity(0.40f); }

    public static GlassMaterialConfig statCard()   { return regular(28f); }
    public static GlassMaterialConfig featureCard(){ return regular(26f); }
    public static GlassMaterialConfig aboutCard()  { return regular(24f); }

    public static GlassMaterialConfig listItem()   { return clear(22f); }
    public static GlassMaterialConfig chip()       { return clear(999f); }
    public static GlassMaterialConfig button()     { return clear(22f); }
}
