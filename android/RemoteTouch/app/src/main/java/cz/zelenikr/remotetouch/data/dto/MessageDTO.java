package cz.zelenikr.remotetouch.data.dto;

import android.support.annotation.NonNull;

import java.io.Serializable;

import cz.zelenikr.remotetouch.data.EventType;

/**
 * Represents message for REST server.
 *
 * @author Roman Zelenik
 */
public class MessageDTO implements Serializable {

  private final String id;

  private final Serializable content;

  private final EventType event;

  public MessageDTO(@NonNull String id, @NonNull EventType event) {
    this(id, event, "");
  }

  /**
   * @param id      Client identification token.
   * @param event   Type of message.
   * @param content Content of message.
   */
  public MessageDTO(@NonNull String id, @NonNull EventType event, @NonNull Serializable content) {
    this.content = content;
    this.event = event;
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public Serializable getContent() {
    return content;
  }

  public EventType getEvent() {
    return event;
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
