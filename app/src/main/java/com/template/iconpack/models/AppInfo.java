package com.template.iconpack.models;

/**
 * Represents an installed app on the device.
 */
public class AppInfo {
    public String appName;
    public String packageName;
    public String componentName;
    public boolean isThemed;
    public boolean isSelected;
    public String drawableName;

    public AppInfo(String appName, String packageName, String componentName) {
        this.appName = appName;
        this.packageName = packageName;
        this.componentName = componentName;
        this.isThemed = false;
        this.isSelected = false;
        this.drawableName = null;
    }

    public String getComponentInfo() {
        return "ComponentInfo{" + packageName + "/" + componentName + "}";
    }
}
