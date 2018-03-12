package cz.zelenikr.remotetouch.security.exception;

/**
 * Created by Roman on 11.3.2018.
 */

public class UnsupportedCipherException extends RuntimeException {
    public UnsupportedCipherException(String message) {
        super(message);
    }

    public UnsupportedCipherException(Exception e) {
        super(e);
    }
}
