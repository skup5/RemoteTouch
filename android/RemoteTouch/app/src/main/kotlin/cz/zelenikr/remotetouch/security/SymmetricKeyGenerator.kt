package cz.zelenikr.remotetouch.security

/**
 * @param T type of generated key object
 * @author Roman Zelenik
 */
interface SymmetricKeyGenerator<T> {
    /**
     * @return new random key
     */
    fun generate(): T
}
