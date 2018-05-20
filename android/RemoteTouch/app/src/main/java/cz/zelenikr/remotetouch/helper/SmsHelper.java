package cz.zelenikr.remotetouch.helper;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony.Sms.Inbox;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import cz.zelenikr.remotetouch.data.event.SmsEventContent;

/**
 * @author Roman Zelenik
 */
public final class SmsHelper {

    private static final Uri SMS_INBOX = Uri.parse("content://sms/inbox");
    private static final String
        ADDRESS_COLUMN = "address",
        BODY_COLUMN = "body",
        DATE_COLUMN = "date",
        READ_COLUMN = "read";

    /**
     * Reads all new unread sms from {@link Inbox} provider. If there are not any new sms
     * returns empty list.
     *
     * @param context
     * @return new unread sms like list of {@link SmsEventContent}
     */
    public static List<SmsEventContent> getAllNewSms(@NonNull Context context) {
        final List<SmsEventContent> smsList = new ArrayList<>();

        final String[] columns = {ADDRESS_COLUMN, BODY_COLUMN, DATE_COLUMN, READ_COLUMN};
        final String select = READ_COLUMN + " = 0";
        final String sort = DATE_COLUMN + " desc";
        final Cursor managedCursor = context.getContentResolver().query(SMS_INBOX, columns, select, null, sort);

        if (managedCursor == null) {
            return smsList;
        }

        final int numberIndex = managedCursor.getColumnIndex(ADDRESS_COLUMN);
        final int textIndex = managedCursor.getColumnIndex(BODY_COLUMN);
        final int dateIndex = managedCursor.getColumnIndex(DATE_COLUMN);
        final boolean contactsAccessEnabled = SettingsHelper.isContactsReadingEnabled(context);

        String numberStr, text, smsDateStr, name;
        long smsDayTime;

        // Loop through rows
        while (managedCursor.moveToNext()) {

            // Read row values
            numberStr = managedCursor.getString(numberIndex);
            text = managedCursor.getString(textIndex);
            smsDateStr = managedCursor.getString(dateIndex);

            // Process values
            name = contactsAccessEnabled ? ContactHelper.findContactDisplayNameByNumber(context, numberStr, "") : "";
            smsDayTime = Long.valueOf(smsDateStr);

            // Add into the list
            smsList.add(new SmsEventContent(name, numberStr, text, smsDayTime));
        }

        managedCursor.close();
        return smsList;
    }

    private SmsHelper() {
    }
}
