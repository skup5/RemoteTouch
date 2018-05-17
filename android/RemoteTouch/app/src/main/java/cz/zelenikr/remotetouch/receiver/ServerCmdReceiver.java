package cz.zelenikr.remotetouch.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Parcelable;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cz.zelenikr.remotetouch.data.command.CommandDTO;
import cz.zelenikr.remotetouch.data.event.CallEventContent;
import cz.zelenikr.remotetouch.data.event.EventDTO;
import cz.zelenikr.remotetouch.data.event.EventType;
import cz.zelenikr.remotetouch.data.event.NotificationEventContent;
import cz.zelenikr.remotetouch.data.event.SmsEventContent;
import cz.zelenikr.remotetouch.data.wrapper.SerializableParcelWrapper;
import cz.zelenikr.remotetouch.helper.ApiHelper;
import cz.zelenikr.remotetouch.helper.CallHelper;
import cz.zelenikr.remotetouch.helper.NotificationHelper;
import cz.zelenikr.remotetouch.helper.SettingsHelper;
import cz.zelenikr.remotetouch.helper.SmsHelper;
import cz.zelenikr.remotetouch.service.FIIDService;
import cz.zelenikr.remotetouch.service.MessageSenderService;
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
        Log.i(TAG, "onReceive");
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
            case CLIENT_CONNECTED:
                onClientConnected(commandDTO);
                break;
            case CLIENT_DISCONNECTED:
                onClientDisconnected(commandDTO);
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

    private void onClientConnected(CommandDTO cmd) {
        SettingsHelper.storeRemoteClientConnected(context, true);

        sendPhoneState();
    }

    private void onClientDisconnected(CommandDTO cmd) {
        SettingsHelper.storeRemoteClientConnected(context, false);
    }

    private void onTest(CommandDTO cmd) {
        cmd.setOutput("processed");
        send(cmd);
    }

    /**
     * Forwards a specific command to the commands sender ({@link ServerCmdSenderService}).
     *
     * @param cmd the given command
     */
    private void send(CommandDTO cmd) {
        Intent intent = new Intent(context, ServerCmdSenderService.class);
        intent.putExtra(ServerCmdSenderService.INTENT_EXTRAS, cmd);
//        context.startService(intent);
        ServerCmdSenderService.enqueueWork(context, intent);
    }

    /**
     * Forwards an array of events to the events sender ({@link MessageSenderService}).
     *
     * @param events the given events
     */
    private void sendEvents(EventDTO[] events) {
        Intent intent = new Intent(context, MessageSenderService.class);
        intent.putExtra(MessageSenderService.INTENT_EXTRA_IS_MSG, true);
        intent.putExtra(MessageSenderService.INTENT_EXTRA_MANY, true);

        ArrayList<SerializableParcelWrapper> wrappers = new ArrayList<>(events.length);
        for (EventDTO eventDTO : events) {
            wrappers.add(new SerializableParcelWrapper(eventDTO));
        }
        intent.putParcelableArrayListExtra(MessageSenderService.INTENT_EXTRA_NAME, wrappers);

        context.startService(intent);
    }

    /**
     * Sends actual phone state (like unread sms and calls) to the remote client.
     */
    private void sendPhoneState() {
        // Get all new events
        List<CallEventContent> callEventContents = SettingsHelper.areCallsEnabled(context) ? CallHelper.getAllNewCalls(context) : Collections.emptyList();
        List<SmsEventContent> smsEventContents = SettingsHelper.areSmsEnabled(context) ? SmsHelper.getAllNewSms(context) : Collections.emptyList();
        List<NotificationEventContent> notificationEventContents;

        if (ApiHelper.checkCurrentApiLevel(23))
            notificationEventContents = SettingsHelper.areNotificationsEnabled(context) ? NotificationHelper.getAllNewNotifications(context) : Collections.emptyList();
        else
            notificationEventContents = Collections.emptyList();

        // Process all gotten events

        List<EventDTO> events = new ArrayList<>(callEventContents.size() + smsEventContents.size() + notificationEventContents.size());

        for (CallEventContent call : callEventContents) {
            events.add(new EventDTO(EventType.CALL, call));
        }

        for (SmsEventContent sms : smsEventContents) {
            events.add(new EventDTO(EventType.SMS, sms));
        }

        for (NotificationEventContent notification : notificationEventContents) {
            events.add(new EventDTO(EventType.NOTIFICATION, notification));
        }

        // Send processed events
        sendEvents(events.toArray(new EventDTO[0]));
    }
}
