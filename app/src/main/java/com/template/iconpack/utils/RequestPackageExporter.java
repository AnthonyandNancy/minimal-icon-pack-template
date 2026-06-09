package com.template.iconpack.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;

import com.template.iconpack.models.AppInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class RequestPackageExporter {

    public static final class Result {
        public final File zipFile;
        public final int count;

        Result(File zipFile, int count) {
            this.zipFile = zipFile;
            this.count = count;
        }
    }

    private static final class RequestItem {
        AppInfo app;
        String component;
        String componentKey;
        String drawableName;
        String originalIconFile;
    }

    private RequestPackageExporter() {
    }

    public static Result createRequestZip(Context context, List<AppInfo> selectedApps) throws Exception {
        if (selectedApps == null || selectedApps.isEmpty()) {
            throw new IllegalArgumentException("请先选择需要申请适配的应用。");
        }

        List<RequestItem> items = buildItems(selectedApps);
        File outputDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (outputDir == null) outputDir = new File(context.getFilesDir(), "icon_requests");
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new IllegalStateException("无法创建导出目录");
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File zipFile = uniqueOutputFile(outputDir, "icon_request_" + timestamp + ".zip");

        try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(zipFile))) {
            putTextEntry(zip, "request_icons.json", buildRequestJson(items).toString(2));
            putTextEntry(zip, "appfilter.xml", buildAppfilterXml(items));

            PackageManager pm = context.getPackageManager();
            for (RequestItem item : items) {
                byte[] png = drawableToPng(loadIcon(context, pm, item.app));
                putBytesEntry(zip, item.originalIconFile, png);
            }
        }

        return new Result(zipFile, items.size());
    }

    private static List<RequestItem> buildItems(List<AppInfo> selectedApps) {
        List<RequestItem> items = new ArrayList<>();
        Set<String> usedIconFiles = new HashSet<>();
        Set<String> usedDrawables = new HashSet<>();

        for (AppInfo app : selectedApps) {
            String activityName = safeText(app.componentName);
            String packageName = safeText(app.packageName);
            String componentKey = packageName + "/" + activityName;

            RequestItem item = new RequestItem();
            item.app = app;
            item.component = "ComponentInfo{" + componentKey + "}";
            item.componentKey = componentKey;
            item.drawableName = uniqueDrawableName(app, usedDrawables);
            item.originalIconFile = "icons/" + uniqueIconFileName(app, componentKey, usedIconFiles);
            items.add(item);
        }
        return items;
    }

    private static JSONObject buildRequestJson(List<RequestItem> items) throws Exception {
        JSONObject root = new JSONObject();
        root.put("version", 1);
        root.put("source", "Icon Pack App Request");
        root.put("createdAt", utcNow());

        JSONArray array = new JSONArray();
        for (RequestItem item : items) {
            JSONObject obj = new JSONObject();
            obj.put("appName", safeText(item.app.appName));
            obj.put("packageName", safeText(item.app.packageName));
            obj.put("activityName", safeText(item.app.componentName));
            obj.put("component", item.component);
            obj.put("componentKey", item.componentKey);
            obj.put("drawableName", item.drawableName);
            obj.put("originalIconFile", item.originalIconFile);
            obj.put("status", "pending");
            array.put(obj);
        }
        root.put("items", array);
        return root;
    }

    private static String buildAppfilterXml(List<RequestItem> items) {
        StringBuilder xml = new StringBuilder();
        xml.append("<resources>\n");
        xml.append("\t<iconback img1=\"iconback\"/>\n");
        xml.append("\t<iconmask img1=\"iconmask\"/>\n");
        xml.append("\t<iconupon img1=\"iconupon\"/>\n");
        xml.append("\t<scale factor=\"1.0\"/>\n\n");
        for (RequestItem item : items) {
            xml.append("\t<!-- ")
                    .append(escapeXml(safeText(item.app.appName)))
                    .append(" -->\n");
            xml.append("\t<item\n")
                    .append("\t\tcomponent=\"")
                    .append(escapeXml(item.component))
                    .append("\"\n")
                    .append("\t\tdrawable=\"")
                    .append(escapeXml(item.drawableName))
                    .append("\"/>\n\n");
        }
        xml.append("</resources>\n");
        return xml.toString();
    }

    private static Drawable loadIcon(Context context, PackageManager pm, AppInfo app) throws Exception {
        try {
            return pm.getActivityIcon(new ComponentName(app.packageName, app.componentName));
        } catch (Exception ignored) {
            try {
                return pm.getApplicationIcon(app.packageName);
            } catch (PackageManager.NameNotFoundException e) {
                return context.getApplicationInfo().loadIcon(pm);
            }
        }
    }

    private static byte[] drawableToPng(Drawable drawable) throws Exception {
        Bitmap bitmap;
        if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
            if (bitmap == null) bitmap = drawToBitmap(drawable);
        } else {
            bitmap = drawToBitmap(drawable);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        return out.toByteArray();
    }

    private static Bitmap drawToBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth() > 0 ? drawable.getIntrinsicWidth() : 192;
        int height = drawable.getIntrinsicHeight() > 0 ? drawable.getIntrinsicHeight() : 192;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private static void putTextEntry(ZipOutputStream zip, String name, String text) throws Exception {
        putBytesEntry(zip, name, text.getBytes(StandardCharsets.UTF_8));
    }

    private static void putBytesEntry(ZipOutputStream zip, String name, byte[] data) throws Exception {
        ZipEntry entry = new ZipEntry(name);
        zip.putNextEntry(entry);
        zip.write(data);
        zip.closeEntry();
    }

    private static File uniqueOutputFile(File dir, String fileName) {
        File file = new File(dir, fileName);
        if (!file.exists()) return file;

        String base = fileName.replaceFirst("\\.zip$", "");
        int index = 1;
        do {
            file = new File(dir, base + "_" + index + ".zip");
            index++;
        } while (file.exists());
        return file;
    }

    private static String uniqueIconFileName(AppInfo app, String componentKey, Set<String> used) {
        String base = sanitizeFileName(safeText(app.appName));
        if (base.isEmpty()) base = sanitizeFileName(safeText(app.packageName));
        if (base.isEmpty()) base = "app";

        String name = base + ".png";
        if (!used.contains(name)) {
            used.add(name);
            return name;
        }

        String hashed = base + "_" + shortHash(componentKey) + ".png";
        used.add(hashed);
        return hashed;
    }

    private static String uniqueDrawableName(AppInfo app, Set<String> used) {
        String source = safeText(app.drawableName);
        if (source.isEmpty()) source = safeText(app.packageName);
        if (source.isEmpty()) source = safeText(app.appName);

        String base = source.toLowerCase(Locale.US)
                .replaceAll("[^a-z0-9_]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_+|_+$", "");
        if (base.isEmpty()) base = "icon_" + shortHash(safeText(app.packageName) + "/" + safeText(app.componentName));
        if (!base.matches("^[a-z].*")) base = "icon_" + base;

        String drawable = base;
        if (used.contains(drawable)) {
            drawable = base + "_" + shortHash(safeText(app.packageName) + "/" + safeText(app.componentName));
        }
        used.add(drawable);
        return drawable;
    }

    private static String sanitizeFileName(String value) {
        return safeText(value)
                .replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]+", "_")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static String shortHash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] bytes = digest.digest(safeText(value).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Math.min(3, bytes.length); i++) {
                sb.append(String.format(Locale.US, "%02x", bytes[i]));
            }
            return sb.toString();
        } catch (Exception e) {
            return Long.toHexString(Math.abs(safeText(value).hashCode()));
        }
    }

    private static String utcNow() {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        return fmt.format(new Date());
    }

    private static String escapeXml(String value) {
        return safeText(value)
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private static String safeText(String value) {
        return value == null ? "" : value.trim();
    }
}
