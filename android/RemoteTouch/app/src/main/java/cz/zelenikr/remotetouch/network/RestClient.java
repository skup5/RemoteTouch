package cz.zelenikr.remotetouch.network;

import java.net.URL;

import cz.zelenikr.remotetouch.data.message.MessageContent;
import cz.zelenikr.remotetouch.data.message.MessageType;

/**
 * Provides messages sending to the REST server.
 *
 * @author Roman Zelenik
 */
public interface RestClient {
    /**
     * Sends a specific text message to the server.
     *
     * @param msg  text content
     * @param path a specific sub domain where {@code msg} should be sent
     * @return true if message was successfully sent, false otherwise
     */
    boolean send(String msg, String path);

    /**
     * Sends a specific structure message to the server.
     *
     * @param content object, which attributes are the message content
     * @param path a specific sub domain where {@code msg} should be sent
     * @return true if message was successfully sent, false otherwise
     */
    boolean send(MessageContent content, String path);

    /**
     * Sets a server address including protocol, domain and port. <p/>
     * For example {@literal http://myserver.com} or {@literal https://myserver.com:443}.
     * @param url a full server address
     */
    void setRestServer(URL url);

    void setClientToken(String token);
}
