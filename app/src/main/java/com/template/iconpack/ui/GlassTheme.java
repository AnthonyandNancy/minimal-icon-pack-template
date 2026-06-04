package com.template.iconpack.ui;

import android.graphics.Color;

/**
 * Unified design tokens for Harmony-style Liquid Glass UI.
 */
public final class GlassTheme {

    private GlassTheme() {}

    // ── Page background gradients ─────────────────────────
    public static final int BG_TOP       = Color.parseColor("#F7FAFF");
    public static final int BG_MID       = Color.parseColor("#F2F5FF");
    public static final int BG_BOTTOM    = Color.parseColor("#EDF8FF");

    // ── Background blobs ──────────────────────────────────
    public static final int BLOB_BLUE         = Color.parseColor("#A7C8FF");
    public static final int BLOB_PURPLE       = Color.parseColor("#CDB7FF");
    public static final int BLOB_CYAN         = Color.parseColor("#BFF4FF");
    public static final float BLOB_BLUE_ALPHA   = 0.28f;
    public static final float BLOB_PURPLE_ALPHA = 0.24f;
    public static final float BLOB_CYAN_ALPHA   = 0.20f;

    // ── Text ──────────────────────────────────────────────
    public static final int TEXT_PRIMARY     = Color.parseColor("#111827");
    public static final int TEXT_SECONDARY   = Color.parseColor("#5F6B7A");
    public static final int TEXT_HINT        = Color.parseColor("#9CA3AF");

    // ── Accent ────────────────────────────────────────────
    public static final int ACCENT_BLUE      = Color.parseColor("#3B82F6");
    public static final int ACCENT_PURPLE    = Color.parseColor("#7C5CFF");
    public static final int ACCENT_CYAN      = Color.parseColor("#2DD4BF");
    public static final int SUCCESS_GREEN    = Color.parseColor("#34C759");
    public static final int WARNING_AMBER    = Color.parseColor("#FFB020");

    // ── Glass surfaces (light mode) ───────────────────────
    public static final int GLASS_CARD_PRIMARY   = Color.parseColor("#B8FFFFFF"); // 72%
    public static final int GLASS_CARD_SECONDARY = Color.parseColor("#99FFFFFF"); // 60%
    public static final int GLASS_TOOLBAR        = Color.parseColor("#D9FFFFFF"); // 85%
    public static final int GLASS_DRAWER_BG      = Color.parseColor("#E6FFFFFF"); // 90%
    public static final int GLASS_DIALOG_BG      = Color.parseColor("#EFFFFFFF"); // 94%
    public static final int GLASS_STROKE_BRIGHT  = Color.parseColor("#EFFFFFFF");
    public static final int GLASS_STROKE_DIM     = Color.parseColor("#66FFFFFF"); // 40%
    public static final int GLASS_SELECTED_BG    = Color.parseColor("#99FFFFFF");

    // ── Dimensions (dp) ───────────────────────────────────
    public static final int DP_PAGE_MARGIN       = 20;
    public static final int DP_CARD_SPACING      = 12;
    public static final int DP_CARD_PADDING      = 16;
    public static final int DP_TOOLBAR_RADIUS    = 34;
    public static final int DP_DRAWER_RADIUS     = 32;
    public static final int DP_CARD_RADIUS       = 24;
    public static final int DP_HERO_RADIUS       = 30;
    public static final int DP_BUTTON_RADIUS     = 999;
    public static final int DP_ICON_CARD_RADIUS  = 20;

    public static final int DP_TOOLBAR_HEIGHT    = 68;
    public static final int DP_HERO_HEIGHT       = 150;
    public static final int DP_STAT_CARD_HEIGHT  = 110;
    public static final int DP_ENTRY_CARD_HEIGHT = 84;
    public static final int DP_MENU_ITEM_HEIGHT  = 52;
    public static final int DP_ICON_GRID_ITEM    = 104;
    public static final int DP_ICON_IMAGE_SIZE   = 54;

    public static final int DP_HERO_ICON_SIZE    = 56;
    public static final int DP_STAT_ICON_CONTAINER = 38;
    public static final int DP_ENTRY_ICON_CONTAINER = 42;
    public static final int DP_STAT_BADGE_HEIGHT = 28;
    public static final int DP_STAT_NUMBER_SIZE  = 26;

    // ── Elevation ─────────────────────────────────────────
    public static final float ELEV_TOOLBAR    = 8f;
    public static final float ELEV_HERO       = 6f;
    public static final float ELEV_CARD       = 4f;
    public static final float ELEV_DRAWER     = 14f;
    public static final float ELEV_DIALOG     = 16f;

    // ── Typography (sp) ───────────────────────────────────
    public static final int SP_TOOLBAR_TITLE  = 22;
    public static final int SP_SECTION_TITLE  = 18;
    public static final int SP_HERO_TITLE     = 22;
    public static final int SP_CARD_TITLE     = 15;
    public static final int SP_BODY           = 14;
    public static final int SP_CAPTION        = 12;
    public static final int SP_STAT_NUMBER    = 26;

    // ── Animation (ms) ────────────────────────────────────
    public static final long ANIM_PAGE_TRANSITION   = 220L;
    public static final long ANIM_CARD_CLICK        = 120L;
    public static final long ANIM_DRAWER_OPEN       = 260L;
    public static final long ANIM_DRAWER_CLOSE      = 220L;
    public static final long ANIM_CARD_STAGGER      = 30L;
    public static final float ANIM_PRESS_SCALE      = 0.975f;
}
