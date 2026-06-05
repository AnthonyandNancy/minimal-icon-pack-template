package com.template.iconpack.ui.glass;

/** Deep Aurora glass. Edge-heavy → glass "pops" while base stays transparent. */
public final class GlassMaterialFactory {

    private GlassMaterialFactory() {}

    public static GlassMaterialConfig clear(float r) {
        return new GlassMaterialConfig().mode(0).radius(r).elevation(3f)
                .base(0x08FFFFFF).stroke(0x50FFFFFF)
                .opacity(0.08f).saturation(0.45f).displacementScale(0.55f)
                .highlight(0.35f).edge(0.55f).shadow(0.25f).innerGlow(0.18f)
                .noise(0.008f).elasticity(0.22f);
    }
    public static GlassMaterialConfig regular(float r) {
        return new GlassMaterialConfig().mode(1).radius(r).elevation(6f)
                .base(0x0CFFFFFF).stroke(0x70FFFFFF)
                .opacity(0.12f).saturation(0.50f).displacementScale(0.65f)
                .highlight(0.45f).edge(0.65f).shadow(0.28f).innerGlow(0.22f)
                .noise(0.012f).elasticity(0.28f);
    }
    public static GlassMaterialConfig prominent(float r) {
        return new GlassMaterialConfig().mode(2).radius(r).elevation(12f)
                .base(0x10FFFFFF).stroke(0x88FFFFFF)
                .opacity(0.16f).saturation(0.55f).displacementScale(0.75f)
                .highlight(0.55f).edge(0.78f).shadow(0.32f).innerGlow(0.26f)
                .noise(0.016f).elasticity(0.34f).aberration(true);
    }

    public static GlassMaterialConfig toolbar()  { return prominent(38f); }
    public static GlassMaterialConfig hero()     { return prominent(34f); }
    public static GlassMaterialConfig drawer()   { return prominent(36f).shadow(0.36f); }
    public static GlassMaterialConfig bottomBar(){ return prominent(36f).opacity(0.20f); }
    public static GlassMaterialConfig statCard()   { return regular(28f); }
    public static GlassMaterialConfig featureCard(){ return regular(26f); }
    public static GlassMaterialConfig aboutCard()  { return regular(24f); }
    public static GlassMaterialConfig listItem()   { return clear(22f); }
    public static GlassMaterialConfig chip()       { return clear(999f); }
    public static GlassMaterialConfig button()     { return clear(22f); }
}
