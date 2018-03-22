package cz.zelenikr.remotetouch.network;

/**
 * Provides secured messages sending to the REST server.
 *
 * @author Roman Zelenik
 */
public interface SecureRestClient extends RestClient {
    void setSecureToken(String secureToken);

    void setSecureKey(String secureKey);
}
