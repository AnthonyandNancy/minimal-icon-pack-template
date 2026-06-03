package com.template.iconpack.models;

/**
 * Represents a single icon drawable entry from drawable.xml.
 */
public class DrawableInfo {
    public String name;
    public int resId;

    public DrawableInfo(String name, int resId) {
        this.name = name;
        this.resId = resId;
    }

    @Override
    public String toString() {
        return name;
    }
}
