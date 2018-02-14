package cz.zelenikr.remotetouch.data;

import java.io.Serializable;

/**
 * Represents message for REST server.
 *
 * @author Roman Zelenik
 */
public class MessageDTO implements Serializable{
    /**
     * client identification token
     */
    public String id;
    /**
     * content of message
     */
    public String content;
    /**
     * type of message
     */
    public EEventType event;

//    public MessageDTO() {
//        this(0, "event");
//    }

    public MessageDTO(String id, EEventType event) {
        this(id, event, "");
    }

    public MessageDTO(String id, EEventType event, String content) {
        this.content = content;
        this.event = event;
        this.id = id;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MessageDTO{");
        sb.append("id=").append(id);
        sb.append(", content='").append(content).append('\'');
        sb.append(", event='").append(event).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
