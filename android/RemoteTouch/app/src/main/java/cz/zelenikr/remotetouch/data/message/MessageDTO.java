package cz.zelenikr.remotetouch.data.message;

import android.support.annotation.NonNull;

import java.io.Serializable;


/**
 * Represents message for REST server.
 *
 * @author Roman Zelenik
 */
public class MessageDTO implements Serializable {

    private final String id;

    private final Serializable content;

    /* remove ??? */
    private final MessageType type;

    public MessageDTO(@NonNull String id, @NonNull MessageType type) {
        this(id, type, "");
    }

    public MessageDTO(@NonNull String id, @NonNull Serializable content) {
        this(id, MessageType.NONE, content);
    }

    /**
     * @param id      Client identification token.
     * @param type    Type of message.
     * @param content Content of message.
     */
    public MessageDTO(@NonNull String id, @NonNull MessageType type, @NonNull Serializable content) {
        this.content = content;
        this.type = type;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Serializable getContent() {
        return content;
    }

    public MessageType getType() {
        return type;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MessageDTO{");
        sb.append("id=").append(id);
        sb.append(", content='").append(content).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
