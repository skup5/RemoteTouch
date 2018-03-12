package cz.zelenikr.remotetouch.security;

/**
 * @param <T> type of generated key object
 * @author Roman Zelenik
 */
public interface SymmetricKeyGenerator<T> {
    /**
     * @return new random key
     */
    T generate();
}
