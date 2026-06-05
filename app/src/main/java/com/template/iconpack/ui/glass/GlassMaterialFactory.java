package com.template.iconpack.ui.glass;

/** Deep Aurora ultra-transparent glass. alpha 0.12-0.20 → background shines through. */
public final class GlassMaterialFactory {

    private GlassMaterialFactory() {}

    public static GlassMaterialConfig clear(float r) {
        return new GlassMaterialConfig().mode(0).radius(r).elevation(3f)
                .base(0x0CFFFFFF).stroke(0x30FFFFFF)
                .opacity(0.12f).saturation(0.30f).displacementScale(0.40f)
                .highlight(0.28f).edge(0.40f).shadow(0.22f).innerGlow(0.14f)
                .noise(0.006f).elasticity(0.20f);
    }
    public static GlassMaterialConfig regular(float r) {
        return new GlassMaterialConfig().mode(1).radius(r).elevation(6f)
                .base(0x14FFFFFF).stroke(0x50FFFFFF)
                .opacity(0.16f).saturation(0.35f).displacementScale(0.55f)
                .highlight(0.38f).edge(0.56f).shadow(0.26f).innerGlow(0.18f)
                .noise(0.010f).elasticity(0.26f);
    }
    public static GlassMaterialConfig prominent(float r) {
        return new GlassMaterialConfig().mode(2).radius(r).elevation(12f)
                .base(0x1AFFFFFF).stroke(0x70FFFFFF)
                .opacity(0.20f).saturation(0.40f).displacementScale(0.70f)
                .highlight(0.48f).edge(0.68f).shadow(0.30f).innerGlow(0.22f)
                .noise(0.012f).elasticity(0.32f).aberration(true);
    }

    public static GlassMaterialConfig toolbar()  { return prominent(38f); }
    public static GlassMaterialConfig hero()     { return prominent(34f); }
    public static GlassMaterialConfig drawer()   { return prominent(36f).shadow(0.34f); }
    public static GlassMaterialConfig bottomBar(){ return prominent(36f).opacity(0.24f); }
    public static GlassMaterialConfig statCard()   { return regular(28f); }
    public static GlassMaterialConfig featureCard(){ return regular(26f); }
    public static GlassMaterialConfig aboutCard()  { return regular(24f); }
    public static GlassMaterialConfig listItem()   { return clear(22f); }
    public static GlassMaterialConfig chip()       { return clear(999f); }
    public static GlassMaterialConfig button()     { return clear(22f); }
}
