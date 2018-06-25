package cz.zelenikr.remotetouch.data.event;

import android.support.annotation.NonNull;

/**
 * Represents Notification.
 *
 * @author Roman Zelenik
 */
public class NotificationEventContent implements EventContent {

    private final int id;

    private final String app;

    private final String label;

    private final String title;

    private final String text;

    private final long when;

    /**
     * @param id    An identifier for this notification unique within the application.
     * @param app   Application's package name of the current Notification.
     * @param label Application's label of current Notification. Attribute of &lt;application&gt; tag in AndroidManifest file.
     * @param title Title of the current Notification.
     * @param text  Text of the current Notification.
     * @param when  A timestamp related to the current Notification, in milliseconds since the epoch.
     */
    public NotificationEventContent(int id, @NonNull String app, @NonNull String label, @NonNull String title, @NonNull String text, long when) {
        this.app = app;
        this.id = id;
        this.label = label;
        this.title = title;
        this.text = text;
        this.when = when;
    }

    public int getId() {
        return id;
    }

    public String getApp() {
        return app;
    }

    public String getLabel() {
        return label;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public long getWhen() {
        return when;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NotificationEventContent{");
        sb.append("id=").append(id);
        sb.append(", app='").append(app).append('\'');
        sb.append(", label='").append(label).append('\'');
        sb.append(", title='").append(title).append('\'');
        sb.append(", text='").append(text).append('\'');
        sb.append(", when=").append(when);
        sb.append('}');
        return sb.toString();
    }
}
