package com.template.iconpack.ui;

import android.graphics.Color;

/**
 * Central design token registry for the Liquid Glass theme.
 * All colors, dimensions, and animation parameters are defined here.
 * No logic — just constants.
 */
public final class GlassTheme {

    private GlassTheme() {}

    // ── Background ────────────────────────────────────────
    public static final int BG_TOP       = Color.parseColor("#F7FAFF");
    public static final int BG_MID       = Color.parseColor("#F4F1FF");
    public static final int BG_BOTTOM    = Color.parseColor("#EEF4FF");

    // ── Text ──────────────────────────────────────────────
    public static final int TEXT_PRIMARY     = Color.parseColor("#111827");
    public static final int TEXT_SECONDARY   = Color.parseColor("#6B7280");
    public static final int TEXT_HINT        = Color.parseColor("#9CA3AF");

    // ── Accent ────────────────────────────────────────────
    public static final int ACCENT_BLUE      = Color.parseColor("#4F8BFF");
    public static final int ACCENT_PURPLE    = Color.parseColor("#8B5CFF");
    public static final int SUCCESS_GREEN    = Color.parseColor("#34C759");
    public static final int WARNING_AMBER    = Color.parseColor("#FFB020");
    public static final int ERROR_RED        = Color.parseColor("#FF3B30");

    // ── Glass surfaces (light mode) ───────────────────────
    public static final int GLASS_BG_CARD        = Color.parseColor("#CCFFFFFF"); // 80% white
    public static final int GLASS_BG_TOOLBAR     = Color.parseColor("#D9FFFFFF"); // 85% white
    public static final int GLASS_BG_DRAWER      = Color.parseColor("#E6FFFFFF"); // 90% white
    public static final int GLASS_BG_HERO        = Color.parseColor("#DDFFFFFF"); // 87% white
    public static final int GLASS_STROKE_BRIGHT  = Color.parseColor("#E6FFFFFF");
    public static final int GLASS_STROKE_DIM     = Color.parseColor("#33FFFFFF");
    public static final int GLASS_SELECTED_BG    = Color.parseColor("#99FFFFFF"); // 60% white

    // ── Dark mode surfaces ────────────────────────────────
    public static final int DARK_BG              = Color.parseColor("#0F172A");
    public static final int DARK_CARD            = Color.parseColor("#33FFFFFF");
    public static final int DARK_STROKE          = Color.parseColor("#26FFFFFF");
    public static final int DARK_TEXT_PRIMARY    = Color.parseColor("#F9FAFB");
    public static final int DARK_TEXT_SECONDARY  = Color.parseColor("#CBD5E1");
    public static final int DARK_ACCENT          = Color.parseColor("#7AA2FF");

    // ── Background blobs (light) ──────────────────────────
    public static final int BLOB_BLUE    = Color.parseColor("#BBD7FF");
    public static final int BLOB_PURPLE  = Color.parseColor("#D7C7FF");
    public static final int BLOB_CYAN    = Color.parseColor("#C9F3FF");

    public static final float BLOB_BLUE_ALPHA      = 0.35f;
    public static final float BLOB_PURPLE_ALPHA    = 0.30f;
    public static final float BLOB_CYAN_ALPHA      = 0.25f;

    // ── Dimensions ────────────────────────────────────────
    public static final int DP_PAGE_MARGIN       = 16;
    public static final int DP_CARD_SPACING      = 12;
    public static final int DP_CARD_PADDING      = 16;
    public static final int DP_CARD_RADIUS_SMALL = 18;
    public static final int DP_CARD_RADIUS_LARGE = 26;
    public static final int DP_TOOLBAR_RADIUS    = 28;
    public static final int DP_LOGO_RADIUS       = 24;
    public static final int DP_BUTTON_RADIUS     = 999;
    public static final int DP_ICON_CARD_RADIUS  = 20;
    public static final int DP_DIALOG_RADIUS     = 30;
    public static final int DP_DRAWER_TOP_RADIUS = 32;

    public static final int DP_HERO_HEIGHT        = 170;
    public static final int DP_TOOLBAR_HEIGHT     = 64;
    public static final int DP_STAT_CARD_HEIGHT   = 118;
    public static final int DP_ENTRY_CARD_HEIGHT  = 92;
    public static final int DP_MENU_ITEM_HEIGHT   = 52;
    public static final int DP_ICON_GRID_ITEM     = 108;
    public static final int DP_ICON_IMAGE_SIZE    = 54;

    // ── Elevation ─────────────────────────────────────────
    public static final float ELEV_CARD       = 6f;
    public static final float ELEV_TOOLBAR    = 10f;
    public static final float ELEV_DRAWER     = 14f;
    public static final float ELEV_DIALOG     = 18f;

    // ── Typography (sp) ───────────────────────────────────
    public static final int SP_PAGE_TITLE     = 24;
    public static final int SP_TOOLBAR_TITLE  = 20;
    public static final int SP_CARD_TITLE     = 16;
    public static final int SP_BODY           = 14;
    public static final int SP_CAPTION        = 12;
    public static final int SP_STAT_NUMBER    = 28;

    // ── Animation ─────────────────────────────────────────
    public static final long ANIM_PAGE_TRANSITION   = 220L;
    public static final long ANIM_CARD_CLICK        = 110L;
    public static final long ANIM_DRAWER_OPEN       = 260L;
    public static final long ANIM_CARD_STAGGER      = 35L;

    public static final float ANIM_PRESS_SCALE      = 0.975f;
}
