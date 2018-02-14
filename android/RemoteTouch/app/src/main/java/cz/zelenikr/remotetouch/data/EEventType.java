package cz.zelenikr.remotetouch.data;

/**
 * @author Roman Zelenik
 */
public enum EEventType {
  CALL, SMS, NOTIFICATION;

  @Override
  public String toString() {
    return super.toString().toLowerCase();
  }
}
