package com.template.iconpack.models;

/**
 * Represents a single icon drawable entry from drawable.xml.
 */
public class DrawableInfo {
    public String name;   // drawable resource name (for image lookup)
    public int resId;
    public String label;  // display name (app name)，fallback to name if null

    public DrawableInfo(String name, int resId) {
        this.name = name;
        this.resId = resId;
        this.label = name;
    }

    public DrawableInfo(String name, int resId, String label) {
        this.name = name;
        this.resId = resId;
        this.label = (label != null && !label.isEmpty()) ? label : name;
    }

    @Override
    public String toString() {
        return label;
    }
}
