package com.template.iconpack.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Simple SharedPreferences wrapper for app settings.
 */
public class PreferencesHelper {

    private static final String PREF_NAME = "icon_pack_prefs";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_SHOW_ICON_NAME = "show_icon_name";
    private static final String KEY_ICON_COLUMNS = "icon_columns";
    private static final String KEY_LAST_SEEN_CHANGELOG_VERSION_CODE = "last_seen_changelog_version_code";

    private final SharedPreferences prefs;

    public PreferencesHelper(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isDarkMode() {
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }

    public void setDarkMode(boolean dark) {
        prefs.edit().putBoolean(KEY_DARK_MODE, dark).apply();
    }

    public boolean isShowIconName() {
        return prefs.getBoolean(KEY_SHOW_ICON_NAME, true);
    }

    public void setShowIconName(boolean show) {
        prefs.edit().putBoolean(KEY_SHOW_ICON_NAME, show).apply();
    }

    public int getIconColumns() {
        return prefs.getInt(KEY_ICON_COLUMNS, 4);
    }

    public void setIconColumns(int columns) {
        prefs.edit().putInt(KEY_ICON_COLUMNS, columns).apply();
    }

    public int getLastSeenChangelogVersionCode() {
        return prefs.getInt(KEY_LAST_SEEN_CHANGELOG_VERSION_CODE, 0);
    }

    public void setLastSeenChangelogVersionCode(int versionCode) {
        prefs.edit().putInt(KEY_LAST_SEEN_CHANGELOG_VERSION_CODE, versionCode).apply();
    }

    public void clearCache() {
        prefs.edit().clear().apply();
    }
}
