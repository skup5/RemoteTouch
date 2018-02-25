package cz.zelenikr.remotetouch.data.dto;

import android.support.annotation.NonNull;

import java.io.Serializable;

import cz.zelenikr.remotetouch.data.EEventType;

/**
 * @author Roman Zelenik
 */
public class EventDTO implements Serializable {

  private EEventType type;

  private EventContent content;

  public EventDTO(@NonNull EEventType type, @NonNull EventContent content) {
    this.type = type;
    this.content = content;
  }

  public EEventType getType() {
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
