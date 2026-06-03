package com.template.iconpack.models;

/**
 * Represents a preset style entry from presets.json.
 */
public class PresetInfo {
    public String id;
    public String name;
    public String iconShape;
    public String background;

    public PresetInfo(String id, String name, String iconShape, String background) {
        this.id = id;
        this.name = name;
        this.iconShape = iconShape;
        this.background = background;
    }
}
