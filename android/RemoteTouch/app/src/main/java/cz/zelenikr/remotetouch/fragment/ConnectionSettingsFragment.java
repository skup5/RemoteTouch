package cz.zelenikr.remotetouch.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.URLUtil;

import cz.zelenikr.remotetouch.R;
import cz.zelenikr.remotetouch.helper.ConnectionHelper;

/**
 * Contains network and connection settings of application.
 *
 * @author Roman Zelenik
 */
public class ConnectionSettingsFragment extends PreferenceFragmentCompat
    implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = ConnectionSettingsFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

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
    public void onStart() {
        super.onStart();
        onTryConnect();
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_connection, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connection_try_connect:
                onTryConnect();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.Key_Connection_Server))) {
            onServerUrlPrefChanged(findPreference(key));
        }
    }

    private String getServerUrlValue() {
        EditTextPreference pref = (EditTextPreference) findPreference(getString(R.string.Key_Connection_Server));
        return pref.getText();
    }

    private void onServerUrlPrefChanged(Preference preference) {
        EditTextPreference etPref = (EditTextPreference) preference;
        etPref.setSummary(etPref.getText());
    }

    private void onTryConnect() {
        new Thread(() -> {
            String address = getServerUrlValue();
            boolean success = ConnectionHelper.isConnected(getContext()) && ConnectionHelper.tryServer(getContext(), address);
            int resId = success ?
                R.string.Preferences_Connection_Server_Url_Try_connect_result_ok :
                R.string.Preferences_Connection_Server_Url_Try_connect_result_fail;
            String msg = getString(resId, address);
            snackbar(msg, Snackbar.LENGTH_LONG);
            Log.d(TAG, "onTryConnect: " + success);
        }).start();
    }

    private boolean validateServerUrl(String newValue) {
        return URLUtil.isNetworkUrl(newValue);
    }

    private void snackbar(String msg, int duration) {
        Snackbar.make(getView(), msg, duration).show();
    }
}
