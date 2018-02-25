package cz.zelenikr.remotetouch.data.dto;

import android.support.annotation.NonNull;

import java.io.Serializable;

import cz.zelenikr.remotetouch.data.EEventType;

/**
 * Represents message for REST server.
 *
 * @author Roman Zelenik
 */
public class MessageDTO implements Serializable {

  private final String id;

  private final Serializable content;

  private final EEventType event;

  public MessageDTO(@NonNull String id, @NonNull EEventType event) {
    this(id, event, "");
  }

  /**
   * @param id      Client identification token.
   * @param event   Type of message.
   * @param content Content of message.
   */
  public MessageDTO(@NonNull String id, @NonNull EEventType event, @NonNull Serializable content) {
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

  public EEventType getEvent() {
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
