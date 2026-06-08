package com.template.iconpack.models;

import java.util.List;

/** Single icon entry from iconpack_categories.json */
public class IconCategoryEntry {
    public String drawableName;
    public String displayName;
    public String packageName;
    public String activityName;
    public String component;
    public String componentKey;
    public List<String> categoryIds;
    public String primaryCategoryId;
    public String matchStatus;
}
