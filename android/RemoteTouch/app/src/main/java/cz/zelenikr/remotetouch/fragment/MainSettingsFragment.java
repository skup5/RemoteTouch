package cz.zelenikr.remotetouch.fragment;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
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
import cz.zelenikr.remotetouch.data.command.Command;
import cz.zelenikr.remotetouch.data.command.CommandDTO;
import cz.zelenikr.remotetouch.helper.AndroidAppComponentHelper;
import cz.zelenikr.remotetouch.helper.ApiHelper;
import cz.zelenikr.remotetouch.helper.NotificationHelper;
import cz.zelenikr.remotetouch.helper.PermissionHelper;
import cz.zelenikr.remotetouch.helper.SettingsHelper;
import cz.zelenikr.remotetouch.receiver.CallReceiver;
import cz.zelenikr.remotetouch.receiver.ServerCmdReceiver;
import cz.zelenikr.remotetouch.receiver.SmsReceiver;
import cz.zelenikr.remotetouch.service.MessageSenderService;

/**
 * Contains basic application settings (and controls).
 *
 * @author Roman Zelenik
 */
public class MainSettingsFragment extends PreferenceFragmentCompat
    implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = MainSettingsFragment.class.getSimpleName();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        startEventService();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences_main from an XML resource
        setPreferencesFromResource(R.xml.preferences_main, rootKey);

        Preference preference = findPreference(getString(R.string.Key_Device_Name));
        if (preference != null) updateDeviceNamePrefSummary(preference);
        preference = findPreference(getString(R.string.Key_RemoteClient_Connected));
        if (preference != null) onRemoteClientPrefChanged(preference);
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
            if (preference.isEnabled()) onNotificationsEnabledClick(preference);
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
            SwitchPreferenceCompat preference = (SwitchPreferenceCompat) findPreference(key);
            turnOnOffAlerts(preference);
        } else if (key.equals(getString(R.string.Key_Calls_Enabled))) {
            SwitchPreferenceCompat preference = (SwitchPreferenceCompat) findPreference(key);
            setReceiverEnabled(CallReceiver.class, preference.isChecked());
        } else if (key.equals(getString(R.string.Key_Sms_Enabled))) {
            SwitchPreferenceCompat preference = (SwitchPreferenceCompat) findPreference(key);
            setReceiverEnabled(SmsReceiver.class, preference.isChecked());
        } else if (key.equals(getString(R.string.Key_Device_Pair_key))) {
            onPairKeyPrefChanged(findPreference(key));
        } else if (key.equals(getString(R.string.Key_Device_Name))) {
            onDeviceNamePrefChanged(findPreference(key));
        } else if (key.equals(getString(R.string.Key_RemoteClient_Connected))) {
            onRemoteClientPrefChanged(findPreference(key));
        }
    }

    private void onCallsEnabledClick(Preference preference) {
        SwitchPreferenceCompat SwitchPreferenceCompat = (SwitchPreferenceCompat) preference;
        if (SwitchPreferenceCompat.isChecked()) {
            checkCallsPermissions(SwitchPreferenceCompat);
            checkContactsPermissions();
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
            checkContactsPermissions();
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
            .setIcon(R.drawable.ic_screen_lock_portrait)
            .setTitle(preference.getTitle())
            .setMessage(preference.getSummary())
            .setPositiveButton(R.string.Actions_Close, (dialog, bt) -> {
            })
            .create()
            .show();
    }

    private void onPairKeyBtClick(View view) {
//        Log.i(TAG, "onPairKeyBtClick: ");
        View root = view.getRootView();
        String newKey = SettingsHelper.regeneratePairKey(getContext());
        TextView pairKeyValue = root.findViewById(R.id.pairKeyValue);
        pairKeyValue.setText(newKey);
    }

    private void onDeviceNamePrefChanged(Preference preference) {
        updateDeviceNamePrefSummary(preference);
        SettingsHelper.refreshToken(getContext());
        sendUpdateFCMToken();
    }

    private void onPairKeyPrefChanged(Preference preference) {
        sendUpdateFCMToken();
    }

    private void onRemoteClientPrefChanged(Preference preference) {
        int summaryRes = SettingsHelper.isRemoteClientConnected(getContext()) ?
            R.string.Preferences_Device_RemoteClient_Summary_on :
            R.string.Preferences_Device_RemoteClient_Summary_off;
        preference.setSummary(summaryRes);
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
                .setMessage(R.string.Check_Calling_Permissions)
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
                .setMessage(R.string.Check_Sms_Permissions)
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
                .setMessage(R.string.Check_NotificationListener_Permission)
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

    /**
     * If isn't granted, shows information dialog to user.
     *
     * @return true if already granted, false otherwise
     */
    private boolean checkContactsPermissions() {
        if (!PermissionHelper.areContactsPermissionsGranted(getContext())) {
            new AlertDialog.Builder(getContext())
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.Application_Name)
                .setMessage(R.string.Check_Contacts_Permissions)
                .setPositiveButton(
                    R.string.Actions_OK,
                    (dialog, which) -> PermissionHelper.requestContactsPermissions(this)
                )
                .setNegativeButton(R.string.Actions_No, (dialog, which) -> {

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

    private void setServiceEnabled(@NonNull Class<? extends Service> service, boolean enabled) {
        Log.i(TAG, "setServiceEnabled: " + enabled);
        ComponentName component = new ComponentName(getContext(), service);
        AndroidAppComponentHelper.setComponentEnabled(getContext(), component, enabled);
    }

    private void startEventService() {
        if (ApiHelper.checkCurrentApiLevel(Build.VERSION_CODES.O)) {
            getActivity().startForegroundService(new Intent(getContext(), MessageSenderService.class));
        } else {
            getActivity().startService(new Intent(getContext(), MessageSenderService.class));
        }
    }

    private void stopEventService() {
        getActivity().stopService(new Intent(getContext(), MessageSenderService.class));
    }

    private void sendUpdateFCMToken() {
        Intent intent = new Intent(getContext(), ServerCmdReceiver.class);
        intent.putExtra(ServerCmdReceiver.INTENT_EXTRAS, new CommandDTO(Command.FCM_SIGN_UP));
        getContext().sendBroadcast(intent);
    }

    private void turnOnOffAlerts(SwitchPreferenceCompat preference) {
        boolean turnOn = preference.isChecked();
        setServiceEnabled(MessageSenderService.class, turnOn);
        if (turnOn) {
            startEventService();
        } else {
            stopEventService();
        }
    }

    private void updateDeviceNamePrefSummary(Preference preference) {
        EditTextPreference pref = (EditTextPreference) preference;
        preference.setSummary(pref.getText());
    }
}
