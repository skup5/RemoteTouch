package cz.zelenikr.remotetouch.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import cz.zelenikr.remotetouch.R;
import cz.zelenikr.remotetouch.data.AppInfo;
import cz.zelenikr.remotetouch.helper.SettingsHelper;

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
    public void onItemStateChanged(AppInfo item, int position) {
        Log.i(TAG, "onItemStateChanged: " + item);
        Set<String> appSet = loadAppsPreference();
        if (item.isSelected()) {
            appSet.add(item.getAppPackage());
        } else {
            appSet.remove(item.getAppPackage());
        }
        saveAppsPreference(appSet);
    }

    @Override
    public void onItemsStateChanged(List<AppInfo> items) {
        Log.i(TAG, "onItemsStateChanged: " + items.size() + " items");
        // Save preference changes asynchronously
        new MultiAppsPreferenceSaver().setAppInfos(items).execute(this);
    }

    private Set<String> loadAppsPreference() {
        return SettingsHelper.getNotificationsApps(getContext());
    }

    private void saveAppsPreference(Set<String> apps) {
        String key = getString(R.string.Key_Notifications_Selected_apps);
        PreferenceManager.getDefaultSharedPreferences(getContext())
            .edit()
            .putStringSet(key, apps)
            .apply();
    }

    private static class MultiAppsPreferenceSaver extends AsyncTask<NotificationSettingsFragment, Void, Void> {
        private List<AppInfo> appInfos = Collections.emptyList();

        @Override
        protected Void doInBackground(NotificationSettingsFragment... fragments) {
            NotificationSettingsFragment settingsFragment = fragments[0];
            Set<String> appSet = settingsFragment.loadAppsPreference();
            for (AppInfo appInfo : appInfos) {
                if (appInfo.isSelected()) {
                    appSet.add(appInfo.getAppPackage());
                } else {
                    appSet.remove(appInfo.getAppPackage());
                }
            }
            settingsFragment.saveAppsPreference(appSet);
            return null;
        }

        public MultiAppsPreferenceSaver setAppInfos(List<AppInfo> appInfos) {
            this.appInfos = appInfos;
            return this;
        }
    }
}
