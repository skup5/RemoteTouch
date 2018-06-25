package cz.zelenikr.remotetouch.processor;

import android.app.Notification;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;

import cz.zelenikr.remotetouch.data.event.NotificationEventContent;
import cz.zelenikr.remotetouch.helper.AndroidAppHelper;
import cz.zelenikr.remotetouch.helper.ApiHelper;

/**
 * This class processes {@link android.service.notification.StatusBarNotification} object
 * and convert it to {@link NotificationEventContent}.
 *
 * @author Roman Zelenik
 */
public class SBNProcessor {

    public NotificationEventContent process(@NonNull Context context, @NonNull StatusBarNotification sbn) {
        final Notification notification = sbn.getNotification();
        final String app = sbn.getPackageName();
        String title = "";
        String text = "";

        // Require API >= 19
        if (ApiHelper.checkCurrentApiLevel(Build.VERSION_CODES.KITKAT)) {
            Bundle extras = notification.extras;
            title = extras.getString(Notification.EXTRA_TITLE, "");
            text = extras.getCharSequence(Notification.EXTRA_TEXT, "").toString();
        }

        return new NotificationEventContent(
            sbn.getId(), app,
            AndroidAppHelper.getAppLabelByPackageName(context, app),
            title, text, notification.when);
    }
}
