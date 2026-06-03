package com.template.iconpack.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.template.iconpack.models.AppInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Scans installed apps and checks against appfilter.xml for themed status.
 */
public class AppScanner {

    /**
     * Scan all installed apps that have a launcher activity.
     * Then cross-reference with appfilter to mark themed/unthemed.
     */
    public static List<AppInfo> scanInstalledApps(Context context) {
        List<AppInfo> apps = new ArrayList<>();
        PackageManager pm = context.getPackageManager();

        // Load appfilter mappings
        Map<String, String> appFilter = IconPackLoader.loadAppFilter(context);

        // Get all launcher activities
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);

        for (ResolveInfo ri : resolveInfos) {
            String packageName = ri.activityInfo.packageName;
            String activityName = ri.activityInfo.name;
            String componentKey = "ComponentInfo{" + packageName + "/" + activityName + "}";

            ApplicationInfo appInfo;
            try {
                appInfo = pm.getApplicationInfo(packageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                continue;
            }

            String appName = pm.getApplicationLabel(appInfo).toString();
            AppInfo app = new AppInfo(appName, packageName, activityName);

            // Check if this app is themed
            app.isThemed = appFilter.containsKey(componentKey);

            apps.add(app);
        }

        // Sort: themed first, then alphabetically
        apps.sort((a, b) -> {
            if (a.isThemed != b.isThemed) return a.isThemed ? -1 : 1;
            return a.appName.compareToIgnoreCase(b.appName);
        });

        return apps;
    }
}
