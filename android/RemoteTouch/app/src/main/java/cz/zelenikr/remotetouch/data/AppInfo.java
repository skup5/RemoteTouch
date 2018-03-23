package cz.zelenikr.remotetouch.data;

import android.graphics.drawable.Drawable;

/**
 * @author Roman Zelenik
 */
public class AppInfo {
    private String appName;
    private String appPackage;
    private Drawable appIcon;
    private boolean isSelected;
    private boolean isSystem;

    public String getAppPackage() {
        return appPackage;
    }

    public void setAppPackage(String appPackage) {
        this.appPackage = appPackage;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public boolean isSystem() {
        return isSystem;
    }

    public void setSystem(boolean system) {
        isSystem = system;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AppInfo{");
        sb.append("appName='").append(appName).append('\'');
        sb.append(", appPackage='").append(appPackage).append('\'');
        sb.append(", appIcon=").append(appIcon);
        sb.append(", isSelected=").append(isSelected);
        sb.append(", isSystem=").append(isSystem);
        sb.append('}');
        return sb.toString();
    }
}
