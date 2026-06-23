package com.template.iconpack.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents one version entry from assets/changelog.json.
 */
public class ChangelogEntry {
    public String versionName;
    public int versionCode;
    public String date;
    public String title;
    public String content;
    public List<String> icons;

    public ChangelogEntry(String versionName, int versionCode, String date,
                          String title, String content, List<String> icons) {
        this.versionName = versionName != null ? versionName : "";
        this.versionCode = versionCode;
        this.date = date != null ? date : "";
        this.title = title != null ? title : "";
        this.content = content != null ? content : "";
        this.icons = icons != null ? icons : new ArrayList<>();
    }
}
