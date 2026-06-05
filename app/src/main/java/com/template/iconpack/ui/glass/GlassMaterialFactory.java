package com.template.iconpack.ui.glass;

/** iOS rainbow background glass presets. */
public final class GlassMaterialFactory {

    private GlassMaterialFactory() {}

    public static GlassMaterialConfig clear(float r) {
        return new GlassMaterialConfig().mode(0).radius(r).elevation(3f)
                .base(0x55FFFFFF).stroke(0x88FFFFFF).opacity(0.30f)
                .saturation(0.30f).displacementScale(0.45f)
                .highlight(0.35f).edge(0.50f).shadow(0.10f).innerGlow(0.20f)
                .noise(0.006f).elasticity(0.22f);
    }
    public static GlassMaterialConfig regular(float r) {
        return new GlassMaterialConfig().mode(1).radius(r).elevation(6f)
                .base(0x72FFFFFF).stroke(0xBFFFFFFF).opacity(0.42f)
                .saturation(0.35f).displacementScale(0.55f)
                .highlight(0.42f).edge(0.60f).shadow(0.12f).innerGlow(0.24f)
                .noise(0.010f).elasticity(0.28f);
    }
    public static GlassMaterialConfig prominent(float r) {
        return new GlassMaterialConfig().mode(2).radius(r).elevation(12f)
                .base(0x88FFFFFF).stroke(0xD9FFFFFF).opacity(0.55f)
                .saturation(0.40f).displacementScale(0.65f)
                .highlight(0.52f).edge(0.72f).shadow(0.14f).innerGlow(0.28f)
                .noise(0.014f).elasticity(0.34f).aberration(true);
    }

    public static GlassMaterialConfig toolbar()  { return prominent(38f); }
    public static GlassMaterialConfig hero()     { return prominent(34f); }
    public static GlassMaterialConfig drawer()   { return prominent(36f).opacity(0.50f); }
    public static GlassMaterialConfig bottomBar(){ return prominent(36f).opacity(0.60f); }
    public static GlassMaterialConfig statCard()   { return regular(28f); }
    public static GlassMaterialConfig featureCard(){ return regular(26f); }
    public static GlassMaterialConfig aboutCard()  { return regular(24f); }
    public static GlassMaterialConfig listItem()   { return clear(22f); }
    public static GlassMaterialConfig chip()       { return clear(999f); }
    public static GlassMaterialConfig button()     { return clear(22f); }
}
