package cz.zelenikr.remotetouch.service;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import cz.zelenikr.remotetouch.data.command.Command;
import cz.zelenikr.remotetouch.data.command.CommandDTO;
import cz.zelenikr.remotetouch.helper.ConnectionHelper;
import cz.zelenikr.remotetouch.helper.SettingsHelper;
import cz.zelenikr.remotetouch.network.JsonSimpleRestClient;
import cz.zelenikr.remotetouch.network.RestClient;

/**
 * @author Roman Zelenik
 */
public class ServerCmdSenderService extends JobIntentService {

    public static final String INTENT_EXTRAS = "cz.zelenikr.remotetouch.ServerCmdSend";

    private static final String TAG = ServerCmdSenderService.class.getSimpleName();
    private static final String REST_PATH_FCM_REG = "/firebase/registration";

    private RestClient restClient;

    @Override
    public void onHandleWork(@NonNull Intent intent) {
        Log.i(TAG, "onReceive: ");
        final Serializable serializableExtra = intent.getSerializableExtra(INTENT_EXTRAS);
        if (serializableExtra == null) {
            Log.w(TAG, "onReceive: CommandDTO is null");
            return;
        }
        if (serializableExtra instanceof CommandDTO) {
            initRestClient(loadClientToken(), loadRestUrl());
            handleServerCmd((CommandDTO) serializableExtra);
        } else {
            Log.w(TAG, "onReceive: " + serializableExtra.getClass().getSimpleName() + " isn't available DTO instance.");
        }

    }

    /**
     * Determines specific rest address and sends required command.
     *
     * @param commandDTO command to send
     */
    private void handleServerCmd(CommandDTO commandDTO) {
        if (commandDTO.getCmd() == Command.TEST) {
            Log.i(TAG, "handleServerCmd: " + commandDTO.toString());
            return;
        }
        send(commandDTO, getRestPathByCmd(commandDTO.getCmd()));
    }

    private void initRestClient(String token, String url) {
        try {
            this.restClient = new JsonSimpleRestClient(token, new URL(url));
        } catch (MalformedURLException e) {
            Log.e(TAG, e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    private String loadClientToken() {
        return SettingsHelper.getToken(this);
    }

    private String loadRestUrl() {
        return SettingsHelper.getServerUrl(this);
    }

    /**
     * Checks internet connection.
     *
     * @return true if device is connected to network
     */
    private boolean isConnected() {
        return ConnectionHelper.isConnected(this);
    }

    /**
     * Determines sub domain where a rest request should be sent by the specific {@link Command}.
     *
     * @param cmd the given command
     * @return sub domain of the rest request
     */
    private String getRestPathByCmd(Command cmd) {
        switch (cmd) {
            case FCM_SIGN_UP:
                return REST_PATH_FCM_REG;
            default:
                return "";
        }
    }

    /**
     * Sends the specific command to the rest server.
     *
     * @param data     command to send
     * @param restPath sub domain of rest request
     * @return true if command was successfully sent
     */
    private boolean send(CommandDTO data, String restPath) {
        if (isConnected()) {
            return restClient.send(data, restPath);
        } else {
            Log.i(TAG, "send: device is not connected to network");
        }
        return false;
    }
}
