package cz.zelenikr.remotetouch.network;

import cz.zelenikr.remotetouch.data.EEventType;
import cz.zelenikr.remotetouch.data.dto.EventContent;

/**
 * Provides messages sending to the REST server.
 *
 * @author Roman Zelenik
 */
public interface RestClient {
  /**
   * Sends a specific event text message to the server.
   *
   * @param msg   text content
   * @param event event type of message
   * @return true if message was successfully sent, false otherwise
   */
  boolean send(String msg, EEventType event);

  /**
   * Sends a specific event message to the server.
   *
   * @param content object, which attributes are the message content
   * @param event   event type of message
   * @return true if message was successfully sent, false otherwise
   */
  boolean send(EventContent content, EEventType event);

}
