package com.template.iconpack.ui;

import android.graphics.Color;

/**
 * Unified design tokens — Harmony-style Liquid Glass on dark-toned background.
 */
public final class GlassTheme {

    private GlassTheme() {}

    // ── Page background (darker, blue-grey to purple to cyan) ──
    public static final int BG_TOP    = Color.parseColor("#CBD7EA");
    public static final int BG_MID    = Color.parseColor("#DAD6EC");
    public static final int BG_BOTTOM = Color.parseColor("#C9E3E9");

    // ── Background blobs (deeper, softer) ──────────────────
    public static final int BLOB_BLUE   = Color.parseColor("#7DB7FF");
    public static final int BLOB_PURPLE = Color.parseColor("#A989FF");
    public static final int BLOB_CYAN   = Color.parseColor("#4EDDD2");
    public static final float BLOB_BLUE_ALPHA   = 0.22f;
    public static final float BLOB_PURPLE_ALPHA = 0.20f;
    public static final float BLOB_CYAN_ALPHA   = 0.16f;

    // ── Text ──────────────────────────────────────────────
    public static final int TEXT_PRIMARY   = Color.parseColor("#111827");
    public static final int TEXT_SECONDARY = Color.parseColor("#5F6B7A");
    public static final int TEXT_HINT      = Color.parseColor("#9CA3AF");

    // ── Accent ────────────────────────────────────────────
    public static final int ACCENT_BLUE   = Color.parseColor("#3B82F6");
    public static final int ACCENT_PURPLE = Color.parseColor("#7C5CFF");
    public static final int SUCCESS_GREEN = Color.parseColor("#34C759");
    public static final int WARNING_AMBER = Color.parseColor("#FFB020");

    // ── Glass surfaces (on dark background, these pop) ────
    public static final int GLASS_TOOLBAR_BG     = Color.parseColor("#B8FFFFFF"); // 72%
    public static final int GLASS_HERO_BG        = Color.parseColor("#8FFFFFFF"); // 56%
    public static final int GLASS_STAT_BG        = Color.parseColor("#75FFFFFF"); // 46%
    public static final int GLASS_ENTRY_BG       = Color.parseColor("#99FFFFFF"); // 60%
    public static final int GLASS_DRAWER_BG      = Color.parseColor("#E6FFFFFF"); // 90%
    public static final int GLASS_BTN_BG         = Color.parseColor("#80FFFFFF"); // 50%
    public static final int GLASS_HIGHLIGHT      = Color.parseColor("#66FFFFFF"); // 40%
    public static final int GLASS_STROKE_BRIGHT  = Color.parseColor("#99FFFFFF");
    public static final int GLASS_STROKE         = Color.parseColor("#80FFFFFF");
    public static final int GLASS_STROKE_DIM     = Color.parseColor("#66FFFFFF");

    // ── Dimensions (dp) ───────────────────────────────────
    public static final int DP_PAGE_MARGIN       = 24;
    public static final int DP_CARD_SPACING       = 16;
    public static final int DP_TOOLBAR_RADIUS     = 34;
    public static final int DP_HERO_RADIUS        = 30;
    public static final int DP_STAT_RADIUS        = 26;
    public static final int DP_ENTRY_RADIUS       = 24;
    public static final int DP_BTN_RADIUS         = 22;
    public static final int DP_BADGE_RADIUS       = 14;

    public static final int DP_TOOLBAR_HEIGHT     = 68;
    public static final int DP_HERO_HEIGHT        = 160;
    public static final int DP_STAT_CARD_HEIGHT   = 132;
    public static final int DP_ENTRY_CARD_HEIGHT  = 88;
    public static final int DP_ICON_GRID_ITEM     = 104;

    public static final float ELEV_TOOLBAR  = 6f;
    public static final float ELEV_HERO     = 8f;
    public static final float ELEV_STAT     = 4f;
    public static final float ELEV_ENTRY    = 3f;

    public static final int SP_TOOLBAR_TITLE = 22;
    public static final int SP_SECTION_TITLE = 18;
    public static final int SP_HERO_NAME     = 22;
    public static final int SP_HERO_META     = 13;
    public static final int SP_STAT_NUMBER   = 30;
    public static final int SP_STAT_LABEL    = 13;
    public static final int SP_ENTRY_TITLE   = 16;
    public static final int SP_ENTRY_DESC    = 12;

    public static final long ANIM_CARD_CLICK = 110L;
    public static final float ANIM_PRESS_SCALE = 0.97f;

    // ── Backward-compat aliases for GlassDrawableFactory / GlassAnimations ──
    public static final int GLASS_CARD_PRIMARY = GLASS_ENTRY_BG;
    public static final int GLASS_TOOLBAR      = GLASS_TOOLBAR_BG;
    public static final int GLASS_SELECTED_BG  = Color.parseColor("#99FFFFFF");
    public static final long ANIM_PAGE_TRANSITION = 220L;
    public static final long ANIM_CARD_STAGGER    = 30L;
    public static final long ANIM_DRAWER_OPEN     = 260L;
}
