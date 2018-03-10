package cz.zelenikr.remotetouch.helper;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

/**
 * This helper class allows to change Android application components
 * ({@link android.app.Activity},
 * {@link android.content.BroadcastReceiver},
 * {@link android.app.Service} and
 * {@link android.content.ContentProvider}) settings.
 *
 * @author Roman Zelenik
 */
public final class AndroidAppComponentHelper {

    private static final String TAG = AndroidAppComponentHelper.class.getSimpleName();

    /**
     * Set the enabled setting for a package component (activity, receiver, service, provider).
     * This setting will override any enabled state which may have been set
     * by the component in its manifest.
     *
     * @param component The component to enable/disable.
     * @param enabled   The required state.
     */
    public static void setComponentEnabled(@NonNull Context context, @NonNull ComponentName component, boolean enabled) {
        int state = enabled ?
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        context.getPackageManager()
            .setComponentEnabledSetting(component, state, PackageManager.DONT_KILL_APP);
    }

    private AndroidAppComponentHelper() {
    }
}
