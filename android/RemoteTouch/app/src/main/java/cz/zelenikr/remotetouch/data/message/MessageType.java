package cz.zelenikr.remotetouch.data.message;

/**
 * @author Roman Zelenik
 */
public enum MessageType {
    EVENT, FIREBASE, NONE;

    public String toString() {
        return super.toString().toLowerCase();
    }

}
