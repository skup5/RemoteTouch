package cz.zelenikr.remotetouch.helper;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;

/**
 * This helper class simplifies contacts operations.
 *
 * @author Roman Zelenik
 */
public final class ContactHelper {

    /**
     * Finds contact with a specific number in phone contacts. If the contact was found, returns his 'display name'.
     *
     * @param context
     * @param number       the required contact number
     * @param defaultValue is returned, if contact was not found or has not any 'display name'
     * @return if contact was found, returns his 'display name', otherwise defaultValue
     */
    public static String findContactDisplayNameByNumber(@NonNull Context context, @NonNull String number, String defaultValue) {
        if (number.isEmpty()) return defaultValue;

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String name = defaultValue;

        ContentResolver contentResolver = context.getContentResolver();
        Cursor contactLookup = contentResolver.query(uri, new String[]{BaseColumns._ID,
            ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

        try {
            if (contactLookup != null && contactLookup.getCount() > 0) {
                contactLookup.moveToNext();
                name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                //String contactId = contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
            }
        } finally {
            if (contactLookup != null) {
                contactLookup.close();
            }
        }

        return name;
    }

    private ContactHelper() {
    }
}
