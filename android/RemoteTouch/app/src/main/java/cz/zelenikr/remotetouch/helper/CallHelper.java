package cz.zelenikr.remotetouch.helper;

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import cz.zelenikr.remotetouch.data.CallType;
import cz.zelenikr.remotetouch.data.event.CallEventContent;

/**
 * @author Roman Zelenik
 */
public final class CallHelper {

    /**
     * Reads all new records from {@link CallLog} provider. If there are not any new records
     * returns empty list.
     *
     * @param context
     * @return new call log records like list of {@link CallEventContent}
     */
    public static List<CallEventContent> getAllNewCalls(@NonNull Context context) {
        final List<CallEventContent> calls = new ArrayList<>();

        final String[] columns = {CallLog.Calls.NUMBER, CallLog.Calls.TYPE, CallLog.Calls.DATE, CallLog.Calls.IS_READ};
        final String select = CallLog.Calls.IS_READ + " = 0";
        final String sort = CallLog.Calls.DATE + " desc";
        final Cursor managedCursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, columns, select, null, sort);

        if (managedCursor == null) {
            return calls;
        }

        final int numberIndex = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        final int typeIndex = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        final int dateIndex = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        final boolean contactsAccessEnabled = SettingsHelper.isContactsReadingEnabled(context);

        CallType callType;
        String numberStr, callTypeStr, callDateStr, name;
        long callDayTime;
        int callTypeIndicator;

        // Loop through rows
        while (managedCursor.moveToNext()) {

            // Read row values
            numberStr = managedCursor.getString(numberIndex);
            callTypeStr = managedCursor.getString(typeIndex);
            callDateStr = managedCursor.getString(dateIndex);

            // Process values

            name = contactsAccessEnabled ? ContactHelper.findContactDisplayNameByNumber(context, numberStr, "") : "";
            callDayTime = Long.valueOf(callDateStr);
            callTypeIndicator = Integer.parseInt(callTypeStr);
            switch (callTypeIndicator) {
                case CallLog.Calls.OUTGOING_TYPE:
                    callType = CallType.OUTGOING;
                    break;

                case CallLog.Calls.INCOMING_TYPE:
                    callType = CallType.INCOMING;
                    break;

                case CallLog.Calls.MISSED_TYPE:
                    callType = CallType.MISSED;
                    break;
                default:
                    callType = null;
            }

            // It's known call type
            if (callType != null) {
                // Add into the list
                calls.add(new CallEventContent(name, numberStr, callType, callDayTime));
            }
        }

        managedCursor.close();
        return calls;
    }

    private CallHelper() {
    }
}
