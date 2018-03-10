package cz.zelenikr.remotetouch.fragment;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import cz.zelenikr.remotetouch.R;
import cz.zelenikr.remotetouch.helper.AndroidAppComponentHelper;
import cz.zelenikr.remotetouch.helper.PermissionHelper;
import cz.zelenikr.remotetouch.receiver.CallReceiver;

/**
 * Contains basic application settings.
 *
 * @author Roman Zelenik
 */
public class SettingsFragment extends PreferenceFragmentCompat
    implements PreferenceFragmentCompat.OnPreferenceStartScreenCallback, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.preferences, rootKey);
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
    public Fragment getCallbackFragment() {
        return this;
    }


    @Override
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat caller, PreferenceScreen pref) {
        caller.setPreferenceScreen(pref);
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        String key = preference.getKey();
        if (key.equals(getString(R.string.Key_Calls_Enabled))) {
            if (preference.isEnabled()) onCallsEnabledClick(preference);
        } else if (key.equals(getString(R.string.Key_Notifications_Enabled))) {

        } else if (key.equals(getString(R.string.Key_Sms_Enabled))) {

        } else if (key.equals(getString(R.string.Key_Pair_key))) {
            onPairKeyClick(preference);
        }

        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionsResult");
        switch (requestCode) {
            case PermissionHelper.MY_PERMISSIONS_REQUEST_CALLING: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "onRequestPermissionsResult: permissions granted");
                } else {
                    Log.i(TAG, "onRequestPermissionsResult: permissions denied");
                    // Set 'Calls disabled'
                    SwitchPreference preference = (SwitchPreference) findPreference(getString(R.string.Key_Calls_Enabled));
                    preference.setChecked(false);
                }
                return;
            }
            default:
                break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.Key_Calls_Enabled))) {
            SwitchPreference preference = (SwitchPreference) findPreference(key);
            setReceiverEnabled(CallReceiver.class, preference.isChecked());
        }
    }


    private void onCallsEnabledClick(Preference preference) {
        SwitchPreference switchPreference = (SwitchPreference) preference;
        if (switchPreference.isChecked()) {
            checkCallsPermissions(switchPreference);
        }
    }

    private void onPairKeyClick(Preference preference) {
        Toast.makeText(getContext(), preference.getTitle(), Toast.LENGTH_SHORT).show();
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        final View dialogView = getLayoutInflater().inflate(R.layout.pair_key_dialog, null);
        dialogBuilder
            .setView(dialogView)
            .setTitle(preference.getTitle())
            .setMessage(preference.getSummary())
            .setPositiveButton(R.string.Actions_OK, (dialog, bt) -> {
            })
            .create()
            .show();
    }

    /**
     * If isn't granted, shows information dialog to user.
     *
     * @return true if already granted, false otherwise
     */
    private boolean checkCallsPermissions(SwitchPreference preference) {
        if (!PermissionHelper.areCallingPermissionsGranted(getContext())) {
            new AlertDialog.Builder(getContext())
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.Application_Name)
                .setMessage(R.string.check_calling_permissions)
                .setPositiveButton(
                    R.string.Actions_OK,
                    (dialog, which) -> PermissionHelper.requestCallingPermissions(this)
                )
                .setNegativeButton(R.string.Actions_No, (dialog, which) -> {
                    preference.setChecked(false);
                })
                .show();
            return false;
        }
        return true;
    }

    private void setReceiverEnabled(@NonNull Class<? extends BroadcastReceiver> receiver, boolean enabled) {
        Log.i(TAG, "setReceiverEnabled: " + enabled);
        ComponentName component = new ComponentName(getContext(), receiver);
        AndroidAppComponentHelper.setComponentEnabled(getContext(), component, enabled);
    }

}
