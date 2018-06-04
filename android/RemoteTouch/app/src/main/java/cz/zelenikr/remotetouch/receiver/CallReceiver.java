package cz.zelenikr.remotetouch.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

import cz.zelenikr.remotetouch.data.CallType;
import cz.zelenikr.remotetouch.data.event.CallEventContent;
import cz.zelenikr.remotetouch.data.event.EventContent;
import cz.zelenikr.remotetouch.data.event.EventDTO;
import cz.zelenikr.remotetouch.data.event.EventType;
import cz.zelenikr.remotetouch.helper.ContactHelper;
import cz.zelenikr.remotetouch.helper.SettingsHelper;
import cz.zelenikr.remotetouch.service.MessageSenderService;

/**
 * Receives NEW_OUTGOING_CALL and PHONE_STATE actions from the system. <br/>
 * From those actions recognizes following events:
 * <ul>
 * <li>{@link #onEndedCall(Context)}</li>
 * <li>{@link #onIncomingCall(Context)}</li>
 * <li>{@link #onMissedCall(Context)}</li>
 * <li>{@link #onOngoingCall(Context)}</li>
 * <li>{@link #onOutgoingCall(Context)}</li>
 * </ul>
 *
 * @author Roman Zelenik
 */
public class CallReceiver extends BroadcastReceiver {

    private static final String TAG = CallReceiver.class.getSimpleName();
    private static final EventType EVENT_TYPE = EventType.CALL;
    private static final boolean SHOW_TOAST = false;
    private static String lastNumber = "", lastName = "";
    private static State lastState = State.IDLE;

    public CallReceiver() {
        super();
        Log.i(TAG, "Was initialized");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        onStateChanged(context, intent);
    }

    private void onStateChanged(Context context, Intent intent) {
        final String intentAction = intent.getAction();
        // It's outgoing call event
        if (intentAction.equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            lastNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            if (lastNumber == null) lastNumber = "";
            updateName(context);
            onOutgoingCall(context);
            lastState = State.DIALING;
        }
        // Some other event
        else {
            final String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            // Someone is calling me
            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                lastNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                if (lastNumber == null) lastNumber = "";
                updateName(context);
                onIncomingCall(context);
                lastState = State.RINGING;
            }
            // I accept the call
            else if ((state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK))) {
                onOngoingCall(context);
                lastState = State.OFFHOOK;
            }
            // The call is over
            else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                if (lastState == State.RINGING) {
                    onMissedCall(context);
                } else if (lastState == State.OFFHOOK) {
                    onEndedCall(context);
                }
                lastState = State.IDLE;
            }
        }
    }

    /**
     * This method is called when ongoing call was ended.
     *
     * @param context
     */
    private void onEndedCall(Context context) {
        Log.i(TAG, "Ended call - " + lastNumber);
        if (SHOW_TOAST)
            Toast.makeText(context, "Ended call - " + lastNumber, Toast.LENGTH_SHORT).show();
        sendEvent(context, CallType.ENDED);
    }

    /**
     * This method is called when user didn't accept a call.
     *
     * @param context
     */
    private void onMissedCall(Context context) {
        Log.i(TAG, "Missed call - " + lastNumber);
        if (SHOW_TOAST)
            Toast.makeText(context, "Missed call - " + lastNumber, Toast.LENGTH_SHORT).show();
        sendEvent(context, CallType.MISSED);
    }

    /**
     * This method is called when user is calling.
     *
     * @param context
     */
    private void onOngoingCall(Context context) {
        Log.i(TAG, "Ongoing call - " + lastNumber);
        if (SHOW_TOAST)
            Toast.makeText(context, "Ongoing call - " + lastNumber, Toast.LENGTH_SHORT).show();
        sendEvent(context, CallType.ONGOING);
    }

    /**
     * This method is called when phone is ringing.
     *
     * @param context
     */
    private void onIncomingCall(Context context) {
        Log.i(TAG, "Incoming call - " + lastNumber);
        if (SHOW_TOAST)
            Toast.makeText(context, "Incoming call - " + lastNumber, Toast.LENGTH_SHORT).show();
        sendEvent(context, CallType.INCOMING);
    }

    /**
     * This method is called when someone (user or app) is dialing.
     *
     * @param context
     */
    private void onOutgoingCall(Context context) {
        Log.i(TAG, "Outgoing call - " + lastNumber);
        if (SHOW_TOAST)
            Toast.makeText(context, "Outgoing call - " + lastNumber, Toast.LENGTH_SHORT).show();
        sendEvent(context, CallType.OUTGOING);
    }

    private static void updateName(Context context) {
        if (!lastNumber.isEmpty() && SettingsHelper.isContactsReadingEnabled(context)) {
            lastName = ContactHelper.findContactDisplayNameByNumber(context, lastNumber, "");
        }
    }

    /**
     * Forwards the new call event to the events sender ({@link MessageSenderService}).
     *
     * @param context
     * @param type    type of the new call event
     */
    private void sendEvent(Context context, CallType type) {
        EventContent content = new CallEventContent(lastName, lastNumber, type, currentTime());
        Intent intent = new Intent(context, MessageSenderService.class);
        intent.putExtra(MessageSenderService.INTENT_EXTRA_IS_MSG, true);
        intent.putExtra(
            MessageSenderService.INTENT_EXTRA_NAME,
            new EventDTO(EVENT_TYPE, content)
        );

        context.startService(intent);
    }

    /**
     * @return the number of milliseconds since January 1, 1970, 00:00:00 GMT
     */
    private long currentTime() {
        return Calendar.getInstance().getTime().getTime();
    }

    private enum State {DIALING, RINGING, OFFHOOK, IDLE}

}