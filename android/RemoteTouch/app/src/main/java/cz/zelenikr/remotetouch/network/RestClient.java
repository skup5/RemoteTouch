package cz.zelenikr.remotetouch.network;

import android.support.annotation.NonNull;

import java.net.URL;

import cz.zelenikr.remotetouch.data.message.MessageContent;

/**
 * Provides messages sending to the REST server.
 *
 * @author Roman Zelenik
 */
public interface RestClient {

    /**
     * Pings server and checks if server is still alive.
     *
     * @return true if response from server was received
     */
    boolean ping();

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
     * @param path    a specific sub domain where {@code content} should be send
     * @return true if message was successfully sent, false otherwise
     */
    boolean send(@NonNull MessageContent content, String path);

    /**
     * Sends a several contents in one specific structure message to the server.
     *
     * @param contents array of specific content objects, which will be send
     * @param path     a specific sub domain where {@code contents} should be sent
     * @return true if message was successfully sent, false otherwise
     */
    boolean sendAll(@NonNull MessageContent[] contents, String path);

    /**
     * Sets a server address including protocol, domain and port. <p/>
     * For example {@literal http://myserver.com} or {@literal https://myserver.com:443}.
     *
     * @param url a full server address
     */
    void setRestServer(URL url);

    void setClientToken(String token);
}
