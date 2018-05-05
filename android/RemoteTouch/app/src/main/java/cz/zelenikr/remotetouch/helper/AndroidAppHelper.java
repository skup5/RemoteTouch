package cz.zelenikr.remotetouch.helper;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cz.zelenikr.remotetouch.R;
import cz.zelenikr.remotetouch.data.AppInfo;

/**
 * The code was got from
 * <a href='https://www.davebennett.tech/android-recyclerview-for-installed-apps-with-checkbox/'>
 * Android Recyclerview for Installed Apps with Checkbox</a>
 */
public final class AndroidAppHelper {

    /**
     * Returns sorted list (by name) of installed apps.
     *
     * @param context
     * @return
     */
    public static List<AppInfo> getApps(@NonNull Context context) {
        return loadApps(context);
    }

    /**
     * @param context
     * @param packageName
     * @return application icon or android system default icon
     */
    public static Drawable getAppIconByPackageName(@NonNull Context context, @NonNull String packageName) {
        Drawable icon;
        try {
            icon = context.getPackageManager().getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            // Get a default icon
            icon = ContextCompat.getDrawable(context, android.R.mipmap.sym_def_app_icon);
        }
        return icon;
    }

    /**
     * @param context
     * @param packageName
     * @return application label or value of 'unknown' text resource
     */
    public static String getAppLabelByPackageName(@NonNull Context context, @NonNull String packageName) {
        String label = context.getString(R.string.Unknown);
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            if (applicationInfo != null) {
                label = packageManager.getApplicationLabel(applicationInfo).toString();
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return label;
    }

    /**
     * Returns the version name of the specific package, as specified by the &lt;manifest&gt; tag's versionName attribute
     * and the version number, as specified by the &lt;manifest&gt; tag's versionCode attribute.
     *
     * @param context
     * @param packageName the given package
     * @return Version name and version code like a values of {@link Pair} object.
     * If package was not found the {@link Pair} object will contain empty string and -1.
     */
    public static Pair<String, Integer> getAppVersionByPackageName(@NonNull Context context, @NonNull String packageName) {
        String name = "";
        int code = -1;
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            name = packageInfo.versionName;
            code = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return new Pair<>(name, code);
    }

    /**
     * Loads all installed apps into the {@code installedApps} list. List is sorted by app name.
     *
     * @param context
     * @return installed apps
     */
    private static List<AppInfo> loadApps(Context context) {
        AppInfo newApp;
        boolean userApp;
        List<ApplicationInfo> packages = context.getPackageManager().getInstalledApplications(0);
        List<AppInfo> installedApps = new ArrayList<>(packages.size());

        for (ApplicationInfo packageInfo : packages) {
            newApp = new AppInfo();
            newApp.setAppName(getAppLabelByPackageName(context, packageInfo.packageName));
            newApp.setAppPackage(packageInfo.packageName);
            newApp.setAppIcon(getAppIconByPackageName(context, packageInfo.packageName));
            userApp = (packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1;
            newApp.setSystem(!userApp);
            installedApps.add(newApp);
        }

        Collections.sort(installedApps, (s1, s2) -> s1.getAppName().compareToIgnoreCase(s2.getAppName()));
        return installedApps;
    }

    private AndroidAppHelper() {
    }
}
