package cz.zelenikr.remotetouch.data.event;

import android.support.annotation.NonNull;

import cz.zelenikr.remotetouch.data.message.MessageContent;

/**
 * @author Roman Zelenik
 */
public class EventDTO implements MessageContent {

    private EventType type;

    private EventContent content;

    public EventDTO(@NonNull EventType type, @NonNull EventContent content) {
        this.type = type;
        this.content = content;
    }

    public EventType getType() {
        return type;
    }

    public EventContent getContent() {
        return content;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EventDTO{");
        sb.append("type=").append(type);
        sb.append(", content=").append(content);
        sb.append('}');
        return sb.toString();
    }
}
