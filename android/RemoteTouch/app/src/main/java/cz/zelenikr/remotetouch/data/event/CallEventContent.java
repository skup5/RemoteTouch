package cz.zelenikr.remotetouch.data.event;

import android.support.annotation.NonNull;

import cz.zelenikr.remotetouch.data.CallType;

/**
 * Represents a call.
 *
 * @author Roman Zelenik
 */
public class CallEventContent implements EventContent {

    private final String name;

    private final String number;

    private final CallType type;

    private final long when;

    /**
     * @param name
     * @param number
     * @param type
     * @param when
     */
    public CallEventContent(@NonNull String name, @NonNull String number, @NonNull CallType type, long when) {
        this.name = name;
        this.number = number;
        this.type = type;
        this.when = when;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public CallType getType() {
        return type;
    }

    public long getWhen() {
        return when;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CallEventContent{");
        sb.append("name='").append(name).append('\'');
        sb.append(", number='").append(number).append('\'');
        sb.append(", type=").append(type);
        sb.append(", when=").append(when);
        sb.append('}');
        return sb.toString();
    }
}
