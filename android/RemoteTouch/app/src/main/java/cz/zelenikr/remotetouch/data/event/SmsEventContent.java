package cz.zelenikr.remotetouch.data.event;

import android.support.annotation.NonNull;

/**
 * Represents SMS.
 *
 * @author Roman Zelenik
 */
public class SmsEventContent implements EventContent {

    private final String name;

    private final String number;

    private final String content;

    private final long when;

    /**
     * @param name    Name of sender/receiver.
     * @param number  Number of sender/receiver.
     * @param content Content of SMS.
     * @param when    Timestamp of sending/receiving in milliseconds since the epoch.
     */
    public SmsEventContent(@NonNull String name, @NonNull String number, @NonNull String content, long when) {
        this.name = name;
        this.number = number;
        this.content = content;
        this.when = when;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public String getContent() {
        return content;
    }

    public long getWhen() {
        return when;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SmsEventContent{");
        sb.append("name='").append(name).append('\'');
        sb.append(", number='").append(number).append('\'');
        sb.append(", content='").append(content).append('\'');
        sb.append(", when=").append(when);
        sb.append('}');
        return sb.toString();
    }
}
