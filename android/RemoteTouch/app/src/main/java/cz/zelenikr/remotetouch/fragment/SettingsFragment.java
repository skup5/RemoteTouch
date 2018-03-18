package cz.zelenikr.remotetouch.fragment;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import cz.zelenikr.remotetouch.R;
import cz.zelenikr.remotetouch.helper.AndroidAppComponentHelper;
import cz.zelenikr.remotetouch.helper.NotificationHelper;
import cz.zelenikr.remotetouch.helper.PermissionHelper;
import cz.zelenikr.remotetouch.helper.SettingsHelper;
import cz.zelenikr.remotetouch.receiver.CallReceiver;
import cz.zelenikr.remotetouch.receiver.SmsReceiver;

/**
 * Contains basic application settings.
 *
 * @author Roman Zelenik
 */
public class SettingsFragment extends PreferenceFragmentCompat
    implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.preferences, rootKey);

        Preference preference = findPreference(getString(R.string.Key_Device_Name));
        if (preference != null) onDeviceNamePrefChanged(preference);
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
    public boolean onPreferenceTreeClick(Preference preference) {
        String key = preference.getKey();
        if (key.equals(getString(R.string.Key_Calls_Enabled))) {
            if (preference.isEnabled()) onCallsEnabledClick(preference);
        } else if (key.equals(getString(R.string.Key_Notifications_Enabled))) {
           // if (preference.isEnabled()) onNotificationsEnabledClick(preference);
        } else if (key.equals(getString(R.string.Key_Sms_Enabled))) {
            if (preference.isEnabled()) onSmsEnabledClick(preference);
        } else if (key.equals(getString(R.string.Key_Device_Pair_key))) {
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
                    SwitchPreferenceCompat preference = (SwitchPreferenceCompat) findPreference(getString(R.string.Key_Calls_Enabled));
                    preference.setChecked(false);
                }
                return;
            }
            case PermissionHelper.MY_PERMISSIONS_REQUEST_SMS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "onRequestPermissionsResult: permissions granted");
                } else {
                    Log.i(TAG, "onRequestPermissionsResult: permissions denied");
                    // Set 'Sms disabled'
                    SwitchPreferenceCompat preference = (SwitchPreferenceCompat) findPreference(getString(R.string.Key_Sms_Enabled));
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
        if (key.equals(getString(R.string.Key_Alerts_Enabled))) {
            //TODO:
        } else if (key.equals(getString(R.string.Key_Calls_Enabled))) {
            SwitchPreferenceCompat preference = (SwitchPreferenceCompat) findPreference(key);
            setReceiverEnabled(CallReceiver.class, preference.isChecked());
        } else if (key.equals(getString(R.string.Key_Sms_Enabled))) {
            SwitchPreferenceCompat preference = (SwitchPreferenceCompat) findPreference(key);
            setReceiverEnabled(SmsReceiver.class, preference.isChecked());
        } else if (key.equals(getString(R.string.Key_Notifications_Enabled))) {
            //TODO:
        } else if (key.equals(getString(R.string.Key_Device_Name))) {
            onDeviceNamePrefChanged(findPreference(key));
        }
    }

    private void onCallsEnabledClick(Preference preference) {
        SwitchPreferenceCompat SwitchPreferenceCompat = (SwitchPreferenceCompat) preference;
        if (SwitchPreferenceCompat.isChecked()) {
            checkCallsPermissions(SwitchPreferenceCompat);
        }
    }

    private void onNotificationsEnabledClick(Preference preference) {
        SwitchPreferenceCompat SwitchPreferenceCompat = (SwitchPreferenceCompat) preference;
        if (SwitchPreferenceCompat.isChecked()) {
            checkNotificationsPermissions(SwitchPreferenceCompat);
        }
    }

    private void onSmsEnabledClick(Preference preference) {
        SwitchPreferenceCompat SwitchPreferenceCompat = (SwitchPreferenceCompat) preference;
        if (SwitchPreferenceCompat.isChecked()) {
            checkSmsPermissions(SwitchPreferenceCompat);
        }
    }

    private void onPairKeyClick(Preference preference) {
        Toast.makeText(getActivity(), preference.getTitle(), Toast.LENGTH_SHORT).show();

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.pair_key_dialog, null);

        Button pairKeyBt = dialogView.findViewById(R.id.pairKeyBt);
        pairKeyBt.setOnClickListener(this::onPairKeyBtClick);
        TextView pairKeyValue = dialogView.findViewById(R.id.pairKeyValue);
        pairKeyValue.setText(SettingsHelper.getPairKey(getActivity()));

        dialogBuilder
            .setView(dialogView)
            .setIcon(R.drawable.ic_action_secure)
            .setTitle(preference.getTitle())
            .setMessage(preference.getSummary())
            .setPositiveButton(R.string.Actions_Close, (dialog, bt) -> {
            })
            .create()
            .show();
    }

    private void onPairKeyBtClick(View view) {
        Log.i(TAG, "onPairKeyBtClick: ");
        View root = view.getRootView();
        String newKey = SettingsHelper.regeneratePairKey(getContext());
        TextView pairKeyValue = root.findViewById(R.id.pairKeyValue);
        pairKeyValue.setText(newKey);
    }

    private void onDeviceNamePrefChanged(Preference preference) {
        EditTextPreference pref = (EditTextPreference) preference;
        preference.setSummary(pref.getText());
    }

    /**
     * If isn't granted, shows information dialog to user.
     *
     * @return true if already granted, false otherwise
     */
    private boolean checkCallsPermissions(SwitchPreferenceCompat preference) {
        if (!PermissionHelper.areCallingPermissionsGranted(getActivity())) {
            new AlertDialog.Builder(getActivity())
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

    /**
     * If isn't granted, shows information dialog to user.
     *
     * @return true if already granted, false otherwise
     */
    private boolean checkSmsPermissions(SwitchPreferenceCompat preference) {
        if (!PermissionHelper.areSmsPermissionsGranted(getContext())) {
            new AlertDialog.Builder(getContext())
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.Application_Name)
                .setMessage(R.string.check_sms_permissions)
                .setPositiveButton(
                    R.string.Actions_OK,
                    (dialog, which) -> PermissionHelper.requestSmsPermissions(this)
                )
                .setNegativeButton(R.string.Actions_No, (dialog, which) -> {
                    preference.setChecked(false);
                })
                .show();
            return false;
        }
        return true;
    }

    /**
     * If isn't granted, shows information dialog to user.
     * User can open system settings and enable NotificationAccessService.
     *
     * @return true if already enabled, false otherwise
     */
    private boolean checkNotificationsPermissions(SwitchPreferenceCompat preference) {
        if (!NotificationHelper.isNotificationListenerEnabled(getContext())) {
            new AlertDialog.Builder(getContext())
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.Application_Name)
                .setMessage(R.string.check_nl_permission)
                .setPositiveButton(
                    R.string.Actions_OK,
                    (dialog, which) -> startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
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