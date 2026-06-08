package com.template.iconpack.models;

import java.util.List;
import java.util.Map;

/** Parsed iconpack_categories.json */
public class IconCategoryData {
    public int version;
    public List<IconCategoryDef> categories;
    public List<IconCategoryEntry> icons;
    /** drawableName → categoryIds */
    public transient Map<String, String[]> iconCategoryMap;
    /** drawableName/componentKey → full category entry */
    public transient Map<String, IconCategoryEntry> iconEntryMap;
    /** category id → category definition */
    public transient Map<String, IconCategoryDef> categoryDefMap;
}
