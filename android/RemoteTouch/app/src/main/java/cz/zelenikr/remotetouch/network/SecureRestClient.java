package cz.zelenikr.remotetouch.network;

/**
 * Provides secured messages sending to the REST server. The content of every message is encrypted.
 *
 * @author Roman Zelenik
 */
public interface SecureRestClient extends RestClient {

    /**
     * Sets a specific message encryption key.
     *
     * @param secureKey encryption key
     */
    void setSecureKey(String secureKey);
}
