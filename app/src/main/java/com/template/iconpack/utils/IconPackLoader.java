package com.template.iconpack.utils;

import android.content.Context;
import android.content.res.XmlResourceParser;

import com.template.iconpack.models.DrawableInfo;
import com.template.iconpack.models.PresetInfo;
import com.template.iconpack.models.WallpaperInfo;

import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
        try {
            // Try res/xml/drawable.xml
            int resId = context.getResources().getIdentifier(
                    "drawable", "xml", context.getPackageName());
            if (resId == 0) return list;

            XmlResourceParser parser = context.getResources().getXml(resId);
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && "drawable".equals(parser.getName())) {
                    String name = parser.getAttributeValue(null, "name");
                    if (name != null) {
                        // Try to find the actual drawable resource
                        int drawableResId = context.getResources().getIdentifier(
                                name, "drawable", context.getPackageName());
                        if (drawableResId == 0) {
                            // Try in drawable-nodpi
                            drawableResId = context.getResources().getIdentifier(
                                    name, "drawable", context.getPackageName());
                        }
                        list.add(new DrawableInfo(name, drawableResId != 0 ? drawableResId : android.R.drawable.ic_menu_gallery));
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
        String title = extractString(obj, "title");
        String thumb = extractString(obj, "thumbnailUrl");
        String download = extractString(obj, "downloadUrl");
        if (id == null) id = "";
        if (title == null) title = "Untitled";
        return new WallpaperInfo(id, title, thumb, download);
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
}
