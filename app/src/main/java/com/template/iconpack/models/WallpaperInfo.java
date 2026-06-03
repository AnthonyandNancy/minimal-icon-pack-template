package com.template.iconpack.models;

/**
 * Represents a wallpaper entry from wallpapers.json.
 */
public class WallpaperInfo {
    public String id;
    public String title;
    public String thumbnailUrl;
    public String downloadUrl;
    public String author;

    public WallpaperInfo(String id, String title, String thumbnailUrl, String downloadUrl) {
        this.id = id;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.downloadUrl = downloadUrl;
        this.author = "";
    }
}
