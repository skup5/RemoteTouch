package cz.zelenikr.remotetouch.network;

import cz.zelenikr.remotetouch.data.message.MessageContent;
import cz.zelenikr.remotetouch.data.message.MessageType;

/**
 * Provides secured messages sending to the REST server. The content of every message is encrypted.
 *
 * @author Roman Zelenik
 */
public interface SecureRestClient extends RestClient {

//    /**
//     * It's method like {@link RestClient#send(MessageContent, MessageType)}, but you can
//     * specify if you want encrypt the content of message.
//     *
//     * @param content
//     * @param type
//     * @param encryptContent
//     * @return
//     */
    // boolean send(MessageContent content, MessageType type, boolean encryptContent);

    /**
     * Sets a specific message encryption key.
     *
     * @param secureKey encryption key
     */
    void setSecureKey(String secureKey);
}
