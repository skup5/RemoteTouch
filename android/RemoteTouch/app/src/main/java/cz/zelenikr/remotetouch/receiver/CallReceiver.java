package cz.zelenikr.remotetouch.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import cz.zelenikr.remotetouch.data.CallType;
import cz.zelenikr.remotetouch.data.EventType;
import cz.zelenikr.remotetouch.data.dto.CallEventContent;
import cz.zelenikr.remotetouch.data.dto.EventDTO;
import cz.zelenikr.remotetouch.helper.ContactHelper;
import cz.zelenikr.remotetouch.service.EventService;

/**
 * @author Roman Zelenik
 */
public class CallReceiver extends BroadcastReceiver {

  private static final String TAG = CallReceiver.class.getSimpleName();
  private static final EventType EVENT_TYPE = EventType.CALL;
  private static String lastNumber = "";
  private static State lastState = State.IDLE;

  public CallReceiver() {
    super();
    Log.i(TAG, "Was initialized");
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    final String intentAction = intent.getAction();
    if (intentAction.equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
      lastNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
      if (lastNumber == null) lastNumber = "";
      onOutgoingCall(context);
      lastState = State.DIALING;
    } else {
      final String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
      if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
        lastNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        if (lastNumber == null) lastNumber = "";
        onIncomingCall(context);
        lastState = State.RINGING;
      } else if ((state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK))) {
        onOngoingCall(context);
        lastState = State.OFFHOOK;
      } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
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
    Toast.makeText(context, "Ended call - " + lastNumber, Toast.LENGTH_SHORT).show();
    final String name = ContactHelper.findContactDisplayNameByNumber(context, lastNumber, "");
    sendEvent(context, new CallEventContent(name, lastNumber, CallType.ENDED));
  }

  /**
   * This method is called when user didn't accept a call.
   *
   * @param context
   */
  private void onMissedCall(Context context) {
    Log.i(TAG, "Missed call - " + lastNumber);
    Toast.makeText(context, "Missed call - " + lastNumber, Toast.LENGTH_SHORT).show();
    final String name = ContactHelper.findContactDisplayNameByNumber(context, lastNumber, "");
    sendEvent(context, new CallEventContent(name, lastNumber, CallType.MISSED));
  }

  /**
   * This method is called when user is calling.
   *
   * @param context
   */
  private void onOngoingCall(Context context) {
    Log.i(TAG, "Ongoing call - " + lastNumber);
    Toast.makeText(context, "Ongoing call - " + lastNumber, Toast.LENGTH_SHORT).show();
    final String name = ContactHelper.findContactDisplayNameByNumber(context, lastNumber, "");
    sendEvent(context, new CallEventContent(name, lastNumber, CallType.ONGOING));
  }

  /**
   * This method is called when phone is ringing.
   *
   * @param context
   */
  private void onIncomingCall(Context context) {
    Log.i(TAG, "Incoming call - " + lastNumber);
    Toast.makeText(context, "Incoming call - " + lastNumber, Toast.LENGTH_SHORT).show();
    final String name = ContactHelper.findContactDisplayNameByNumber(context, lastNumber, "");
    sendEvent(context, new CallEventContent(name, lastNumber, CallType.INCOMING));
  }

  /**
   * This method is called when someone (user or app) is dialing.
   *
   * @param context
   */
  private void onOutgoingCall(Context context) {
    Log.i(TAG, "Outgoing call - " + lastNumber);
    Toast.makeText(context, "Outgoing call - " + lastNumber, Toast.LENGTH_SHORT).show();
    final String name = ContactHelper.findContactDisplayNameByNumber(context, lastNumber, "");
    sendEvent(context, new CallEventContent(name, lastNumber, CallType.OUTGOING));
  }

  private void sendEvent(Context context, CallEventContent content) {
    Intent intent = new Intent(context, EventService.class);
    intent.putExtra(EventService.INTENT_EXTRA_EVENT, true);
    intent.putExtra(
        EventService.INTENT_EXTRA_NAME,
        new EventDTO(EVENT_TYPE, content)
    );

    context.startService(intent);
  }

  private enum State {DIALING, RINGING, OFFHOOK, IDLE}

}