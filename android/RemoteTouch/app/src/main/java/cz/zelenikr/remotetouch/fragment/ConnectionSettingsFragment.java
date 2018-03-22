package cz.zelenikr.remotetouch.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.webkit.URLUtil;

import cz.zelenikr.remotetouch.R;

/**
 * Contains network and connection settings of application.
 *
 * @author Roman Zelenik
 */
public class ConnectionSettingsFragment extends PreferenceFragmentCompat
    implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_connection, rootKey);

        Preference serverUrlPref = findPreference(getString(R.string.Key_Connection_Server));
        if (serverUrlPref != null) {
            serverUrlPref.setOnPreferenceChangeListener((preference, newValue) -> validateServerUrl((String) newValue));
            onServerUrlPrefChanged(serverUrlPref);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.Key_Connection_Server))) {
            onServerUrlPrefChanged(findPreference(key));
        }
    }

    private void onServerUrlPrefChanged(Preference preference) {
        EditTextPreference etPref = (EditTextPreference) preference;
        etPref.setSummary(etPref.getText());
    }

    private boolean validateServerUrl(String newValue) {
        return URLUtil.isNetworkUrl(newValue);
    }
}
