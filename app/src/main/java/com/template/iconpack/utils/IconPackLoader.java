package com.template.iconpack.utils;

import android.content.Context;
import android.content.res.XmlResourceParser;

import com.template.iconpack.models.DrawableInfo;
import com.template.iconpack.models.ChangelogEntry;
import com.template.iconpack.models.IconCategoryData;
import com.template.iconpack.models.IconCategoryDef;
import com.template.iconpack.models.IconCategoryEntry;
import com.template.iconpack.models.PresetInfo;
import com.template.iconpack.models.WallpaperInfo;

import org.xmlpull.v1.XmlPullParser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads icon pack resources: drawable.xml, appfilter.xml, wallpapers.json, presets.json.
 */
public class IconPackLoader {

    /**
     * Parse res/xml/drawable.xml to get list of all icon names.
     * Returns empty list if file is empty or missing.
     */
    public static List<DrawableInfo> loadDrawables(Context context) {
        List<DrawableInfo> list = new ArrayList<>();
        IconCategoryData categoryData = loadIconPackCategories(context);
        try {
            // Try res/xml/drawable.xml
            int resId = context.getResources().getIdentifier(
                    "drawable", "xml", context.getPackageName());
            if (resId == 0) return list;

            XmlResourceParser parser = context.getResources().getXml(resId);
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String tagName = parser.getName();
                    String name = null;
                    if ("item".equals(tagName)) {
                        name = parser.getAttributeValue(null, "drawable");
                    } else if ("drawable".equals(tagName)) {
                        name = parser.getAttributeValue(null, "name");
                    }

                    if (name != null && !name.isEmpty()) {
                        String label = parser.getAttributeValue(null, "label");
                        int drawableResId = context.getResources().getIdentifier(
                                name, "drawable", context.getPackageName());
                        if (drawableResId == 0) {
                            drawableResId = context.getResources().getIdentifier(
                                    name, "drawable-nodpi", context.getPackageName());
                        }
                        label = resolveDrawableLabel(name, label, categoryData);
                        if (label != null && !label.isEmpty()) {
                            list.add(new DrawableInfo(name,
                                    drawableResId != 0 ? drawableResId : android.R.drawable.ic_menu_gallery,
                                    label));
                        } else {
                            list.add(new DrawableInfo(name, drawableResId != 0 ? drawableResId : android.R.drawable.ic_menu_gallery));
                        }
                    }
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            // Empty or invalid XML — return empty list
        }
        return list;
    }

    /**
     * Parse res/xml/appfilter.xml to get ComponentInfo -> drawable name mapping.
     */
    public static Map<String, String> loadAppFilter(Context context) {
        Map<String, String> map = new HashMap<>();
        try {
            int resId = context.getResources().getIdentifier(
                    "appfilter", "xml", context.getPackageName());
            if (resId == 0) return map;

            XmlResourceParser parser = context.getResources().getXml(resId);
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && "item".equals(parser.getName())) {
                    String component = parser.getAttributeValue(null, "component");
                    String drawable = parser.getAttributeValue(null, "drawable");
                    if (component != null && drawable != null) {
                        map.put(component, drawable);
                    }
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            // Empty or invalid
        }
        return map;
    }

    /**
     * Parse assets/wallpapers.json.
     */
    public static List<WallpaperInfo> loadWallpapers(Context context) {
        List<WallpaperInfo> list = new ArrayList<>();
        try {
            InputStream is = context.getAssets().open("wallpapers.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            String json = sb.toString().trim();

            // Simple JSON parsing (no Gson/Jackson dependency)
            list = parseWallpapersJson(json);
        } catch (Exception e) {
            // File missing or empty
        }
        return list;
    }

    /**
     * Parse assets/presets.json.
     */
    public static List<PresetInfo> loadPresets(Context context) {
        List<PresetInfo> list = new ArrayList<>();
        try {
            InputStream is = context.getAssets().open("presets.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            String json = sb.toString().trim();

            list = parsePresetsJson(json);
        } catch (Exception e) {
            // Use default presets
        }
        if (list.isEmpty()) {
            list.add(new PresetInfo("default_circle", "默认圆形", "circle", "auto"));
            list.add(new PresetInfo("rounded_square", "圆角矩形", "rounded_square", "auto"));
            list.add(new PresetInfo("dark_bg", "深色背景", "circle", "dark"));
            list.add(new PresetInfo("light_bg", "浅色背景", "circle", "light"));
        }
        return list;
    }

    /**
     * Parse assets/changelog.json.
     * Supports both {"items": []} and a direct [] root.
     */
    public static List<ChangelogEntry> loadChangelog(Context context) {
        List<ChangelogEntry> list = new ArrayList<>();
        try {
            String json = readAssetText(context, "changelog.json").trim();
            if (json.isEmpty()) return list;

            Object root = new JSONTokener(json).nextValue();
            JSONArray items = null;
            if (root instanceof JSONObject) {
                items = ((JSONObject) root).optJSONArray("items");
            } else if (root instanceof JSONArray) {
                items = (JSONArray) root;
            }
            if (items == null) return list;

            for (int i = 0; i < items.length(); i++) {
                JSONObject obj = items.optJSONObject(i);
                if (obj == null) continue;
                list.add(parseChangelogEntry(obj));
            }
            Collections.sort(list, (a, b) -> Integer.compare(b.versionCode, a.versionCode));
        } catch (Exception e) {
            list.clear();
        }
        return list;
    }

    private static ChangelogEntry parseChangelogEntry(JSONObject obj) {
        List<String> icons = new ArrayList<>();
        JSONArray iconArray = obj.optJSONArray("icons");
        if (iconArray != null) {
            for (int i = 0; i < iconArray.length(); i++) {
                String icon = iconArray.optString(i, "");
                if (!icon.isEmpty()) icons.add(icon);
            }
        }
        return new ChangelogEntry(
                obj.optString("versionName", ""),
                obj.optInt("versionCode", 0),
                obj.optString("date", ""),
                obj.optString("title", ""),
                obj.optString("content", ""),
                icons
        );
    }

    private static String readAssetText(Context context, String name) throws Exception {
        InputStream is = context.getAssets().open(name);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append('\n');
        }
        reader.close();
        return sb.toString();
    }

    /**
     * Load assets/iconpack_categories.json.
     * Returns null if file is missing (graceful no-crash fallback).
     */
    public static IconCategoryData loadIconPackCategories(Context context) {
        try {
            java.io.InputStream is = context.getAssets().open("iconpack_categories.json");
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();
            return parseCategoriesJson(sb.toString().trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static IconCategoryData parseCategoriesJson(String json) {
        if (json.isEmpty() || json.equals("{}")) return null;
        IconCategoryData data = new IconCategoryData();
        data.categories = new ArrayList<>();
        data.icons = new ArrayList<>();
        data.iconCategoryMap = new HashMap<>();
        data.iconEntryMap = new HashMap<>();
        data.categoryDefMap = new HashMap<>();

        try {
            JSONObject root = new JSONObject(json);
            data.version = root.optInt("version", 1);

            JSONArray categories = root.optJSONArray("categories");
            if (categories != null) {
                for (int i = 0; i < categories.length(); i++) {
                    JSONObject obj = categories.optJSONObject(i);
                    if (obj == null) continue;
                    IconCategoryDef cat = parseCategoryDef(obj);
                    if (cat != null && cat.enabled) {
                        data.categories.add(cat);
                        data.categoryDefMap.put(cat.id, cat);
                    }
                }
            }
            Collections.sort(data.categories, (a, b) -> Integer.compare(a.sort, b.sort));

            JSONArray icons = root.optJSONArray("icons");
            if (icons != null) {
                for (int i = 0; i < icons.length(); i++) {
                    JSONObject obj = icons.optJSONObject(i);
                    if (obj == null) continue;
                    IconCategoryEntry entry = parseIconEntry(obj);
                    if (entry == null) continue;
                    data.icons.add(entry);
                    String[] categoryIds = entry.categoryIds.toArray(new String[0]);
                    if (entry.drawableName != null && !entry.drawableName.isEmpty()) {
                        data.iconCategoryMap.put(entry.drawableName, categoryIds);
                        data.iconEntryMap.put(entry.drawableName, entry);
                    }
                    if (entry.componentKey != null && !entry.componentKey.isEmpty()) {
                        data.iconCategoryMap.put(entry.componentKey, categoryIds);
                        data.iconEntryMap.put(entry.componentKey, entry);
                    }
                }
            }
            return data;
        } catch (Exception e) {
            return null;
        }
    }

    private static IconCategoryDef parseCategoryDef(JSONObject obj) {
        IconCategoryDef c = new IconCategoryDef();
        c.id = obj.optString("id", "");
        if (c.id.isEmpty()) return null;
        c.name = obj.optString("name", c.id);
        c.color = obj.optString("color", "");
        c.icon = obj.optString("icon", "");
        c.sort = obj.optInt("sort", 0);
        c.enabled = obj.optBoolean("enabled", true);
        return c;
    }

    private static IconCategoryEntry parseIconEntry(JSONObject obj) {
        IconCategoryEntry e = new IconCategoryEntry();
        e.drawableName = obj.optString("drawableName", "");
        e.displayName = obj.optString("displayName", "");
        e.packageName = obj.optString("packageName", "");
        e.activityName = obj.optString("activityName", "");
        e.component = obj.optString("component", "");
        e.componentKey = obj.optString("componentKey", "");
        e.primaryCategoryId = obj.optString("primaryCategoryId", "");
        e.matchStatus = obj.optString("matchStatus", "");
        e.categoryIds = new ArrayList<>();
        JSONArray ids = obj.optJSONArray("categoryIds");
        if (ids != null) {
            for (int i = 0; i < ids.length(); i++) {
                String id = ids.optString(i, "");
                if (!id.isEmpty()) e.categoryIds.add(id);
            }
        }
        return e;
    }

    private static String resolveDrawableLabel(String drawableName, String xmlLabel, IconCategoryData data) {
        if (data != null && data.iconEntryMap != null) {
            IconCategoryEntry entry = data.iconEntryMap.get(drawableName);
            if (entry != null && entry.displayName != null && !entry.displayName.isEmpty()) {
                if (xmlLabel == null || xmlLabel.isEmpty() || drawableName.equals(xmlLabel)) {
                    return entry.displayName;
                }
            }
        }
        return xmlLabel;
    }

    private static void parseCategories(String arr, java.util.List<IconCategoryDef> out) {
        int depth = 0, objStart = -1;
        for (int i = 0; i < arr.length(); i++) {
            if (arr.charAt(i) == '{') { if (depth == 0) objStart = i; depth++; }
            else if (arr.charAt(i) == '}') {
                depth--;
                if (depth == 0 && objStart >= 0) {
                    IconCategoryDef cat = parseCategoryDef(arr.substring(objStart, i + 1));
                    if (cat != null && cat.enabled) out.add(cat);
                    objStart = -1;
                }
            }
        }
    }

    private static IconCategoryDef parseCategoryDef(String obj) {
        IconCategoryDef c = new IconCategoryDef();
        c.id = extractString(obj, "id");
        c.name = extractString(obj, "name");
        c.color = extractString(obj, "color");
        c.icon = extractString(obj, "icon");
        String sort = extractNumber(obj, "sort");
        c.sort = sort != null ? Integer.parseInt(sort) : 0;
        String en = extractString(obj, "enabled");
        c.enabled = en == null || "true".equals(en);
        return c;
    }

    private static void parseIconEntries(String arr, java.util.List<IconCategoryEntry> out) {
        int depth = 0, objStart = -1;
        for (int i = 0; i < arr.length(); i++) {
            if (arr.charAt(i) == '{') { if (depth == 0) objStart = i; depth++; }
            else if (arr.charAt(i) == '}') {
                depth--;
                if (depth == 0 && objStart >= 0) {
                    IconCategoryEntry e = parseIconEntry(arr.substring(objStart, i + 1));
                    if (e != null) out.add(e);
                    objStart = -1;
                }
            }
        }
    }

    private static IconCategoryEntry parseIconEntry(String obj) {
        IconCategoryEntry e = new IconCategoryEntry();
        e.drawableName = extractString(obj, "drawableName");
        e.componentKey = extractString(obj, "componentKey");
        e.primaryCategoryId = extractString(obj, "primaryCategoryId");
        e.matchStatus = extractString(obj, "matchStatus");
        String ids = extractStringArray(obj, "categoryIds");
        e.categoryIds = new java.util.ArrayList<>();
        if (ids != null && !ids.isEmpty() && !ids.equals("[]")) {
            for (String s : ids.replace("[","").replace("]","").split(",")) {
                String tid = s.trim().replace("\"", "");
                if (!tid.isEmpty()) e.categoryIds.add(tid);
            }
        }
        return e;
    }

    private static String extractStringArray(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search); if (idx < 0) return null;
        int colon = json.indexOf(':', idx + search.length()); if (colon < 0) return null;
        int bracket = json.indexOf('[', colon); if (bracket < 0) return null;
        int close = findMatchingBracket(json, bracket);
        return json.substring(bracket, close + 1);
    }

    private static int findMatchingBracket(String s, int start) {
        int depth = 0;
        for (int i = start; i < s.length(); i++) {
            if (s.charAt(i) == '[' || s.charAt(i) == '{') depth++;
            else if (s.charAt(i) == ']' || s.charAt(i) == '}') { depth--; if (depth == 0) return i; }
        }
        return -1;
    }

    private static String extractNumber(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search); if (idx < 0) return null;
        int colon = json.indexOf(':', idx + search.length()); if (colon < 0) return null;
        int start = colon + 1;
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '\n')) start++;
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) end++;
        return end > start ? json.substring(start, end) : null;
    }

    // -- Minimal JSON parsers (no external libs) --

    private static List<WallpaperInfo> parseWallpapersJson(String json) {
        List<WallpaperInfo> list = new ArrayList<>();
        if (json.equals("[]") || json.isEmpty()) return list;

        // Simple array-of-objects parser
        int depth = 0;
        int objStart = -1;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                if (depth == 0) objStart = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && objStart >= 0) {
                    String obj = json.substring(objStart, i + 1);
                    WallpaperInfo w = parseWallpaperObject(obj);
                    if (w != null) list.add(w);
                    objStart = -1;
                }
            }
        }
        return list;
    }

    private static WallpaperInfo parseWallpaperObject(String obj) {
        String id = extractString(obj, "id");
        String title = firstString(obj, "title", "name");
        String thumb = firstString(obj, "thumbnailUrl", "thumbnail", "thumb", "previewUrl",
                "preview", "imageUrl", "image", "url");
        String download = firstString(obj, "downloadUrl", "download", "fullUrl", "full",
                "wallpaperUrl", "wallpaper", "imageUrl", "image", "url");
        if (id == null) id = "";
        if (title == null) title = "Untitled";
        WallpaperInfo wallpaper = new WallpaperInfo(id, title, thumb, download);
        String author = extractString(obj, "author");
        if (author != null) wallpaper.author = author;
        return wallpaper;
    }

    private static List<PresetInfo> parsePresetsJson(String json) {
        List<PresetInfo> list = new ArrayList<>();
        if (json.equals("[]") || json.isEmpty()) return list;

        int depth = 0;
        int objStart = -1;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                if (depth == 0) objStart = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && objStart >= 0) {
                    String obj = json.substring(objStart, i + 1);
                    PresetInfo p = parsePresetObject(obj);
                    if (p != null) list.add(p);
                    objStart = -1;
                }
            }
        }
        return list;
    }

    private static PresetInfo parsePresetObject(String obj) {
        String id = extractString(obj, "id");
        String name = extractString(obj, "name");
        String shape = extractString(obj, "iconShape");
        String bg = extractString(obj, "background");
        return new PresetInfo(id != null ? id : "", name != null ? name : "", shape != null ? shape : "", bg != null ? bg : "");
    }

    private static String extractString(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        int colon = json.indexOf(':', idx + search.length());
        if (colon < 0) return null;
        int quote1 = json.indexOf('"', colon + 1);
        if (quote1 < 0) return null;
        int quote2 = json.indexOf('"', quote1 + 1);
        if (quote2 < 0) return null;
        return json.substring(quote1 + 1, quote2);
    }

    private static String firstString(String json, String... keys) {
        for (String key : keys) {
            String value = extractString(json, key);
            if (value != null && !value.trim().isEmpty()) return value;
        }
        return null;
    }
}
