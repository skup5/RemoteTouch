package cz.zelenikr.remotetouch.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import cz.zelenikr.remotetouch.data.command.Command;
import cz.zelenikr.remotetouch.data.command.CommandDTO;
import cz.zelenikr.remotetouch.helper.ConnectionHelper;
import cz.zelenikr.remotetouch.receiver.ServerCmdReceiver;

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
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_connection, rootKey);

        Preference pref = findPreference(getString(R.string.Key_Connection_Server));
        pref.setOnPreferenceChangeListener((preference, newValue) -> validateServerUrl((String) newValue));
        updateServerUrlPrefSummary(pref);

        pref = findPreference(getString(R.string.Key_Connection_Server_Test));
        pref.setOnPreferenceClickListener(preference -> {
            onTryConnect();
            return true;
        });

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
        updateServerUrlPrefSummary(preference);
        sendUpdateFCMToken();
    }

    private void onTryConnect() {
        new Thread(() -> {
            String address = getServerUrlValue();
            if (ConnectionHelper.isUsedAvailableConnection(getContext())) {
                boolean success = ConnectionHelper.tryServer(getContext(), address);
                int resId = success ?
                    R.string.Preferences_Connection_Server_Url_Try_connect_result_ok :
                    R.string.Preferences_Connection_Server_Url_Try_connect_result_fail;
                String msg = getString(resId);
                snackbar(msg, Snackbar.LENGTH_LONG);
                Log.d(TAG, "onTryConnect: " + success);
            } else {
                snackbar(getString(R.string.NoNetworkConnection), Snackbar.LENGTH_LONG);
            }
        }).start();
    }

    private void sendUpdateFCMToken() {
        Intent intent = new Intent(getContext(), ServerCmdReceiver.class);
        intent.putExtra(ServerCmdReceiver.INTENT_EXTRAS, new CommandDTO(Command.FCM_SIGN_UP));
        getContext().sendBroadcast(intent);
    }

    private void updateServerUrlPrefSummary(Preference preference) {
        EditTextPreference etPref = (EditTextPreference) preference;
        etPref.setSummary(etPref.getText());
    }

    private boolean validateServerUrl(String newValue) {
        return URLUtil.isNetworkUrl(newValue);
    }

    private void snackbar(String msg, int duration) {
        Snackbar.make(getView(), msg, duration).show();
    }
}
