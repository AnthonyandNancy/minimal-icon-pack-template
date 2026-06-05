package com.template.iconpack.ui.glass;

/**
 * Glass presets calibrated for maximum visibility on prismatic backgrounds.
 * Higher opacity + stronger edges + more aggressive chromatic aberration.
 */
public final class GlassMaterialFactory {

    private GlassMaterialFactory() {}

    public static GlassMaterialConfig clear(float r) {
        return new GlassMaterialConfig().mode(0).radius(r).elevation(3f)
                .base(0x55FFFFFF).stroke(0x88FFFFFF)
                .opacity(0.35f).saturation(0.25f).displacementScale(0.35f)
                .highlight(0.32f).edge(0.44f).shadow(0.10f).innerGlow(0.20f)
                .noise(0.008f).elasticity(0.22f);
    }

    public static GlassMaterialConfig regular(float r) {
        return new GlassMaterialConfig().mode(1).radius(r).elevation(6f)
                .base(0x72FFFFFF).stroke(0xBFFFFFFF)
                .opacity(0.48f).saturation(0.30f).displacementScale(0.50f)
                .highlight(0.42f).edge(0.56f).shadow(0.12f).innerGlow(0.25f)
                .noise(0.012f).elasticity(0.28f);
    }

    public static GlassMaterialConfig prominent(float r) {
        return new GlassMaterialConfig().mode(2).radius(r).elevation(12f)
                .base(0x88FFFFFF).stroke(0xE6FFFFFF)
                .opacity(0.62f).saturation(0.35f).displacementScale(0.65f)
                .highlight(0.55f).edge(0.72f).shadow(0.15f).innerGlow(0.30f)
                .noise(0.016f).elasticity(0.34f).aberration(true);
    }

    public static GlassMaterialConfig toolbar()  { return prominent(38f); }
    public static GlassMaterialConfig hero()     { return prominent(34f); }
    public static GlassMaterialConfig drawer()   { return prominent(36f).opacity(0.58f).shadow(0.16f); }
    public static GlassMaterialConfig bottomBar(){ return prominent(36f).opacity(0.66f); }

    public static GlassMaterialConfig statCard()   { return regular(28f); }
    public static GlassMaterialConfig featureCard(){ return regular(26f); }
    public static GlassMaterialConfig aboutCard()  { return regular(24f); }

    public static GlassMaterialConfig listItem()   { return clear(22f); }
    public static GlassMaterialConfig chip()       { return clear(999f); }
    public static GlassMaterialConfig button()     { return clear(22f); }
}
