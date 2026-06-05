package com.template.iconpack.ui.glass;

/**
 * Preset GlassMaterialConfig factory.
 * Three modes: CLEAR (list items, chips), REGULAR (cards, info), PROMINENT (hero, toolbar, drawer).
 */
public final class GlassMaterialFactory {

    private GlassMaterialFactory() {}

    // ── Core modes ───────────────────────────────────

    public static GlassMaterialConfig clear(float radiusDp) {
        return new GlassMaterialConfig()
                .mode(GlassMaterialConfig.MODE_CLEAR)
                .radius(radiusDp).elevation(3f)
                .base(0x40FFFFFF).stroke(0x88FFFFFF)
                .opacity(0.42f)
                .highlight(0.24f).edge(0.32f).shadow(0.10f)
                .noise(0.006f).elasticity(0.22f);
    }

    public static GlassMaterialConfig regular(float radiusDp) {
        return new GlassMaterialConfig()
                .mode(GlassMaterialConfig.MODE_REGULAR)
                .radius(radiusDp).elevation(6f)
                .base(0x66FFFFFF).stroke(0xBFFFFFFF)
                .opacity(0.58f)
                .highlight(0.36f).edge(0.46f).shadow(0.14f)
                .noise(0.010f).elasticity(0.26f);
    }

    public static GlassMaterialConfig prominent(float radiusDp) {
        return new GlassMaterialConfig()
                .mode(GlassMaterialConfig.MODE_PROMINENT)
                .radius(radiusDp).elevation(12f)
                .base(0x88FFFFFF).stroke(0xE6FFFFFF)
                .opacity(0.72f)
                .highlight(0.52f).edge(0.68f).shadow(0.18f)
                .noise(0.014f).elasticity(0.34f)
                .aberration(true);
    }

    // ── Named presets ────────────────────────────────

    public static GlassMaterialConfig toolbar() { return prominent(38f); }
    public static GlassMaterialConfig hero()    { return prominent(34f); }
    public static GlassMaterialConfig drawer() {
        return prominent(36f).opacity(0.68f).shadow(0.22f);
    }
    public static GlassMaterialConfig bottomActionBar() { return prominent(36f).opacity(0.76f); }

    public static GlassMaterialConfig statCard()    { return regular(28f); }
    public static GlassMaterialConfig featureCard() { return regular(26f); }
    public static GlassMaterialConfig aboutCard()   { return regular(24f); }

    public static GlassMaterialConfig listItem()  { return clear(22f); }
    public static GlassMaterialConfig chip()      { return clear(999f); }
    public static GlassMaterialConfig button()    { return clear(22f); }
}
