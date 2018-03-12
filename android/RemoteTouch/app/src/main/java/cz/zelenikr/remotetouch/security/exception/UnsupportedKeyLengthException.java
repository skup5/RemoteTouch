package cz.zelenikr.remotetouch.security.exception;

/**
 * Created by Roman on 11.3.2018.
 */

public class UnsupportedKeyLengthException extends RuntimeException {
    public UnsupportedKeyLengthException(String message) {
        super(message);
    }
}
