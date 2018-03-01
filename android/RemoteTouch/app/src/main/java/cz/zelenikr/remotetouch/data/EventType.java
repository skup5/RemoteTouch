package cz.zelenikr.remotetouch.data;

/**
 * @author Roman Zelenik
 */
public enum EventType {
  CALL, SMS, NOTIFICATION;

  @Override
  public String toString() {
    return super.toString().toLowerCase();
  }
}
