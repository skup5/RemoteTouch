package cz.zelenikr.remotetouch.network;

import cz.zelenikr.remotetouch.data.EEventType;

/**
 * Provides messages sending to the REST server.
 *
 * @author Roman Zelenik
 */
interface RestClient {
  /**
   * Sends a specific event message to the server.
   *
   * @param msg   message content
   * @param event event type of message
   * @return true if message was successfully sent, false otherwise
   */
  boolean send(String msg, EEventType event);
}
