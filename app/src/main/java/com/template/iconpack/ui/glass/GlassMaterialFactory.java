package com.template.iconpack.ui.glass;

/**
 * Deep Aurora dark-mode glass presets.
 * Dark background requires higher base opacity for glass to be visible,
 * while highlights and edges provide the "liquid" refraction feel.
 */
public final class GlassMaterialFactory {

    private GlassMaterialFactory() {}

    // ── Core modes ──────────────────────────────────────

    public static GlassMaterialConfig clear(float radiusDp) {
        return new GlassMaterialConfig()
                .mode(GlassMaterialConfig.MODE_CLEAR)
                .radius(radiusDp).elevation(3f)
                .base(0xFFF8FAFC).stroke(0x66FFFFFF)
                .opacity(0.12f)
                .highlight(0.32f).edge(0.40f).shadow(0.24f)
                .noise(0.012f).elasticity(0.22f);
    }

    public static GlassMaterialConfig regular(float radiusDp) {
        return new GlassMaterialConfig()
                .mode(GlassMaterialConfig.MODE_REGULAR)
                .radius(radiusDp).elevation(6f)
                .base(0xFFF1F5F9).stroke(0x99FFFFFF)
                .opacity(0.18f)
                .highlight(0.44f).edge(0.56f).shadow(0.28f)
                .noise(0.016f).elasticity(0.26f);
    }

    public static GlassMaterialConfig prominent(float radiusDp) {
        return new GlassMaterialConfig()
                .mode(GlassMaterialConfig.MODE_PROMINENT)
                .radius(radiusDp).elevation(12f)
                .base(0xFFE2E8F0).stroke(0xCCFFFFFF)
                .opacity(0.26f)
                .highlight(0.60f).edge(0.76f).shadow(0.34f)
                .noise(0.020f).elasticity(0.34f)
                .aberration(true);
    }

    // ── Named presets ───────────────────────────────────

    public static GlassMaterialConfig toolbar()    { return prominent(38f); }
    public static GlassMaterialConfig hero()       { return prominent(34f); }
    public static GlassMaterialConfig drawer()     { return prominent(36f).opacity(0.24f).shadow(0.32f); }
    public static GlassMaterialConfig bottomBar()  { return prominent(36f).opacity(0.28f); }

    public static GlassMaterialConfig statCard()   { return regular(28f); }
    public static GlassMaterialConfig featureCard(){ return regular(26f); }
    public static GlassMaterialConfig aboutCard()  { return regular(24f); }

    public static GlassMaterialConfig listItem()   { return clear(22f); }
    public static GlassMaterialConfig chip()       { return clear(999f); }
    public static GlassMaterialConfig button()     { return clear(22f); }
}
