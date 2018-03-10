package cz.zelenikr.remotetouch.fragment;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.CallLog;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cz.zelenikr.remotetouch.R;
import cz.zelenikr.remotetouch.data.NotificationWrapper;
import cz.zelenikr.remotetouch.helper.NotificationHelper;
import cz.zelenikr.remotetouch.helper.PermissionHelper;
import cz.zelenikr.remotetouch.storage.NotificationDataStore;
import cz.zelenikr.remotetouch.storage.NotificationDbHelper;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DeveloperFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DeveloperFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DeveloperFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String TAG = DeveloperFragment.class.getSimpleName();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public DeveloperFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DeveloperFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DeveloperFragment newInstance(String param1, String param2) {
        DeveloperFragment fragment = new DeveloperFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        setHasOptionsMenu(true);

        notificationDataStore.open();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_developer, container, false);
        view.findViewById(R.id.callsBt).setOnClickListener(this::onCallsBtClick);
        view.findViewById(R.id.smsBt).setOnClickListener(this::onSmsBtClick);
        view.findViewById(R.id.notificationsBt).setOnClickListener(this::onNotificationsBtClick);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        notificationDataStore = new NotificationDataStore(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    //////  IMPORTED FROM MainActivity /////////////////////////////////////////////////////////////

    private static final Uri SMS_INBOX = Uri.parse("content://sms/inbox");
    private static final Uri CALLS = Uri.parse("content://call_log/calls");

    private NotificationDataStore notificationDataStore;

    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationDataStore.close();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_developer, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_notification) {
            Log.i(TAG, "Add notification");
            NotificationHelper.test(getContext(), (int) System.currentTimeMillis());
            snackbar("Added new notification", Snackbar.LENGTH_SHORT);
            return true;
        } else if (id == R.id.action_export_notification_logs) {
            onExportNotificationLogsBtClick(item.getActionView());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void snackbar(String text, int duration) {
        View view = getView().findViewById(R.id.developerPanel);
        Snackbar.make(view, text, duration).show();
    }

    private void onCallsBtClick(View view) {
        if (!PermissionHelper.checkCallingPermissions(getActivity())) {
            return;
        }

        fillListView(getCallDetails(), "Whole call log");
    }

    private void onSmsBtClick(View view) {
        if (!PermissionHelper.checkSmsPermissions(getActivity())) {
            return;
        }

        List<String> messageList = new ArrayList<>();
        String[] cols = new String[]{"date", "person", "address", "read", "body"};

        // returns received sms ordered by date (and unread)
        Cursor cursor = getActivity().getContentResolver().query(SMS_INBOX, cols,
            "read=0", null, "read, date desc");

        if (cursor == null) {
            messageList.add(getString(R.string.empty));
        } else {
            String message = "";

            while (cursor.moveToNext()) {
                message = cols[0].toUpperCase() + ": " + new Date(Long.valueOf(cursor.getString(0))) + "\n";

                for (int i = 1; i < cols.length; i++)
                    message += cols[i].toUpperCase() + ": " + cursor.getString(i) + "\n";

                messageList.add(message);
            }
        }

        fillListView(messageList, "Received sms ordered by date (and unread)");
    }

    private void onNotificationsBtClick(View view) {
        List<String> notificationList;

        // Load from shared preferences
//    notificationList = loadPreferences(NotificationAccessService.getLocalClassName());

        // Load from sqlite db
        notificationList = loadStoredNotifications();

        if (notificationList.isEmpty())
            notificationList.add(getString(R.string.empty));

        fillListView(notificationList, "Whole notification log sort by date");
    }

    private void onExportNotificationLogsBtClick(View view) {
        if (!PermissionHelper.checkExternalStoragePermissions(getActivity())) {
            return;
        }

        String backupDBPath =
            new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date()) +
                "_" + NotificationDbHelper.DATABASE_NAME;
        File backupDB = new File(Environment.getExternalStorageDirectory(), backupDBPath);
        String resultMessage = "DB " + backupDB.getAbsolutePath();
        if (notificationDataStore.export(backupDB)) {
            resultMessage += " Exported!";
        } else {
            resultMessage += " export FAILED!";
        }
        snackbar(resultMessage, Toast.LENGTH_LONG);
    }

    private List<String> getCallDetails() {
        List<String> calls = new ArrayList<>();
        String callDetail;

        Cursor managedCursor = getActivity().getContentResolver().query(CALLS, null, null, null, "date desc");
        if (managedCursor == null) {
            calls.add(getString(R.string.empty));
            return calls;
        }
        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
        int newCall = managedCursor.getColumnIndex(CallLog.Calls.NEW);

        while (managedCursor.moveToNext()) {
            String phNumber = managedCursor.getString(number);
            String callType = managedCursor.getString(type);
            String callDate = managedCursor.getString(date);
            Date callDayTime = new Date(Long.valueOf(callDate));
            String callDuration = managedCursor.getString(duration);
            int callIsNew = managedCursor.getInt(newCall);
            String dir = null;
            int dircode = Integer.parseInt(callType);
            switch (dircode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    dir = "OUTGOING";
                    break;

                case CallLog.Calls.INCOMING_TYPE:
                    dir = "INCOMING";
                    break;

                case CallLog.Calls.MISSED_TYPE:
                    dir = "MISSED";
                    break;
            }
            callDetail = "\nIs new:---" + (callIsNew == 1 ? "yes" : "no")
                + "\nPhone Number:--- " + phNumber + " \nCall Type:--- "
                + dir + " \nCall Date:--- " + callDayTime
                + " \nCall duration in sec :--- " + callDuration;
            calls.add(callDetail);
        }
        managedCursor.close();
        return calls;

    }

    private List<String> loadPreferences(String className) {
        List<String> preferences = new ArrayList<>();
        Map<String, ?> prefs = getContext().getSharedPreferences(className, MODE_PRIVATE).getAll();
        for (Map.Entry entry : prefs.entrySet()) {
            preferences.add(entry.getKey() + " : " + entry.getValue());
        }
        return preferences;
    }

    private List<String> loadStoredNotifications() {
        List<String> notificationList = new ArrayList<>();
        List<NotificationWrapper> notifications = notificationDataStore.getAll();
        // Sort descending by ID
        Collections.sort(notifications, (o1, o2) -> (int) (o2.getId() - o1.getId()));
        // Map to strings
        for (NotificationWrapper wrapper : notifications) {
            notificationList.add(new Date(wrapper.getTimestamp()).toString() + " " + wrapper.getApplication());
        }

        return notificationList;
    }

    private void fillListView(List<String> items, String headline) {
        ListView list = getView().findViewById(R.id.listView);
        list.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, items));
        if (headline != null) {
            TextView header = getView().findViewById(R.id.listViewHeadline);
            header.setText(headline);
        }
    }

}
