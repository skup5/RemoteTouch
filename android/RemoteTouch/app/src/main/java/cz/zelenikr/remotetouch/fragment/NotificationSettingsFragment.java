package cz.zelenikr.remotetouch.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArraySet;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

import java.util.ArrayList;
import java.util.Set;

import cz.zelenikr.remotetouch.R;
import cz.zelenikr.remotetouch.data.AppInfo;

/**
 * Contains advanced notifications settings.
 * <p/>
 * Activities that contain this fragment MUST implement the
 * {@link OpenFragmentListener} interface to handle opening inner fragment event.
 *
 * @author Roman Zelenik
 */
public class NotificationSettingsFragment extends PreferenceFragmentCompat
    implements PreferenceFragmentCompat.OnPreferenceStartScreenCallback, InstalledAppsFragment.OnListItemStateChangedListener {

    public static final String TAG = NotificationSettingsFragment.class.getSimpleName();

    private OpenFragmentListener openFragmentListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OpenFragmentListener) {
            openFragmentListener = (OpenFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                + " must implement OpenFragmentListener");
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_notification, rootKey);

        Preference appsPref = findPreference(getString(R.string.Key_Notifications_Installed_apps));
        appsPref.setOnPreferenceClickListener(preference -> {
            InstalledAppsFragment fragment = InstalledAppsFragment
                .newInstance(new ArrayList<>(loadAppsPreference()));
            openFragmentListener.openFragment(fragment);
            return true;
        });
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat caller, PreferenceScreen pref) {
        caller.setPreferenceScreen(pref);
        return true;
    }

    @Override
    public Fragment getCallbackFragment() {
        return this;
    }

    @Override
    public void onStateChanged(AppInfo item, int position) {
//        Log.i(TAG, "onStateChanged: " + item);
        Set<String> appSet = loadAppsPreference();
        if (item.isSelected()) {
            appSet.add(item.getAppPackage());
        } else {
            appSet.remove(item.getAppPackage());
        }
        saveAppsPreference(appSet);
    }

    private Set<String> loadAppsPreference() {
        Preference preference = findPreference(getString(R.string.Key_Notifications_Installed_apps));
        return preference.getPersistedStringSet(new ArraySet<>());
    }

    private void saveAppsPreference(Set<String> apps) {
        Preference preference = findPreference(getString(R.string.Key_Notifications_Installed_apps));
        preference.persistStringSet(apps);
    }
}
