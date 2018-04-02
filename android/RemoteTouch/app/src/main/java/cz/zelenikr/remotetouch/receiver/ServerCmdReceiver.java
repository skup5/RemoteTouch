package cz.zelenikr.remotetouch.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.Serializable;

import cz.zelenikr.remotetouch.data.command.CommandDTO;
import cz.zelenikr.remotetouch.service.FIIDService;
import cz.zelenikr.remotetouch.service.ServerCmdSenderService;

/**
 * @author Roman Zelenik
 */
public class ServerCmdReceiver extends BroadcastReceiver {

    public static final String INTENT_EXTRAS = "cz.zelenikr.remotetouch.ServerCmdRec";

    private static final String TAG = ServerCmdReceiver.class.getSimpleName();

    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive: ");
        this.context = context;
        final Serializable serializableExtra = intent.getSerializableExtra(INTENT_EXTRAS);
        if (serializableExtra == null) {
            Log.w(TAG, "onReceive: CommandDTO is null");
            return;
        }
        if (serializableExtra instanceof CommandDTO) {
            handleCmd((CommandDTO) serializableExtra);
        } else {
            Log.w(TAG, "onReceive: " + serializableExtra.getClass().getSimpleName() + " isn't available DTO instance.");
        }
    }

    /**
     * Determines how to process the specific command.
     *
     * @param commandDTO
     */
    private void handleCmd(CommandDTO commandDTO) {
        Log.i(TAG, "received cmd " + commandDTO.toString());
        switch (commandDTO.getCmd()) {
            case FCM_SIGN_UP:
                onFcmSignUp(commandDTO);
                break;
            case TEST:
                onTest(commandDTO);
                break;
            default:
                break;
        }
    }

    private void onFcmSignUp(CommandDTO cmd) {
        String token = FIIDService.getFirebaseToken();
        cmd.setOutput(token);
        send(cmd);
    }

    private void onTest(CommandDTO cmd) {
        cmd.setOutput("processed");
        send(cmd);
    }

    /**
     * Forwards a specific command to the commands sender.
     *
     * @param cmd the given command
     */
    private void send(CommandDTO cmd) {
        Intent intent = new Intent(context, ServerCmdSenderService.class);
        intent.putExtra(ServerCmdSenderService.INTENT_EXTRAS, cmd);
        context.startService(intent);
    }
}
