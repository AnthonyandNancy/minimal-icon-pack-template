package com.template.iconpack.ui.glass;

import android.graphics.Color;

/**
 * Parameterised liquid-glass material configuration.
 *
 * Three built-in modes: CLEAR (light glass), REGULAR (medium), PROMINENT (strong).
 * Use GlassMaterialFactory for presets or construct custom configs.
 */
public class GlassMaterialConfig {

    public static final int MODE_CLEAR     = 0;
    public static final int MODE_REGULAR   = 1;
    public static final int MODE_PROMINENT = 2;

    public int    mode = MODE_REGULAR;
    public float  cornerRadiusDp = 26f;
    public float  elevationDp    = 4f;

    // ── Glass surface ────────────────────────────────
    public int    baseColor   = 0x66FFFFFF;
    public int    strokeColor = 0xBFFFFFFF;
    public float  opacity       = 0.58f;
    public float  blurAmount    = 0.28f;
    public float  saturation    = 0.16f;
    public float  displacementScale = 0.32f;
    public float  aberrationIntensity = 0.16f;

    // ── Highlights & shadows ─────────────────────────
    public float  highlightIntensity    = 0.36f;
    public float  edgeIntensity         = 0.46f;
    public float  bottomShadowIntensity = 0.14f;
    public float  innerGlowIntensity    = 0.15f;

    // ── Texture ──────────────────────────────────────
    public float  noiseIntensity = 0.010f;
    public boolean enableNoise   = true;

    // ── Effects ──────────────────────────────────────
    public boolean enableChromaticAberration = false;
    public boolean enableRenderEffectBlur    = false;

    // ── Motion ──────────────────────────────────────
    public float  elasticity = 0.26f;

    // ── Corner override (for Drawer: right-only corners) ──
    public float[] cornerRadii; // null = uniform cornerRadiusDp

    // ── Convenience builders ─────────────────────────

    public GlassMaterialConfig mode(int m) { this.mode = m; return this; }
    public GlassMaterialConfig radius(float r) { this.cornerRadiusDp = r; return this; }
    public GlassMaterialConfig elevation(float e) { this.elevationDp = e; return this; }
    public GlassMaterialConfig base(int c) { this.baseColor = c; return this; }
    public GlassMaterialConfig stroke(int c) { this.strokeColor = c; return this; }
    public GlassMaterialConfig opacity(float o) { this.opacity = o; return this; }
    public GlassMaterialConfig highlight(float h) { this.highlightIntensity = h; return this; }
    public GlassMaterialConfig edge(float e) { this.edgeIntensity = e; return this; }
    public GlassMaterialConfig shadow(float s) { this.bottomShadowIntensity = s; return this; }
    public GlassMaterialConfig noise(float n) { this.noiseIntensity = n; return this; }
    public GlassMaterialConfig elasticity(float e) { this.elasticity = e; return this; }
    public GlassMaterialConfig corners(float[] c) { this.cornerRadii = c; return this; }
    public GlassMaterialConfig aberration(boolean a) { this.enableChromaticAberration = a; return this; }
    public GlassMaterialConfig saturation(float s) { this.saturation = s; return this; }
    public GlassMaterialConfig displacementScale(float ds) { this.displacementScale = ds; return this; }
    public GlassMaterialConfig innerGlow(float ig) { this.innerGlowIntensity = ig; return this; }
}
