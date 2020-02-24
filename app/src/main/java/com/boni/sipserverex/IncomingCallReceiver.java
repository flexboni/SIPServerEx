package com.boni.sipserverex;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipProfile;
import android.util.Log;
import android.widget.Toast;

/**
 * SIP call 수신.
 */
public class IncomingCallReceiver extends BroadcastReceiver {
    // Call 이벤트가 여기로 들어온다.
    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d("IncomingCallReceiver", "onReceive");

        SipAudioCall inComingCall = null;
        try {
            SipAudioCall.Listener listener = new SipAudioCall.Listener() {
                @Override
                public void onRinging(SipAudioCall call, SipProfile caller) {
                    try {
                        call.answerCall(30);
                    } catch (SipException e) {
                        e.printStackTrace();

                        Toast.makeText(
                                context,
                                "SipManager Listener error\n" + e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
            };

            MainActivity mainActivity = (MainActivity) context;
            inComingCall = mainActivity.sipManager.takeAudioCall(intent, listener);
            inComingCall.answerCall(30);
            inComingCall.startAudio();
            inComingCall.setSpeakerMode(true);

            if (inComingCall.isInCall()) {
                inComingCall.toggleMute();
            }

            mainActivity.call = inComingCall;
            mainActivity.updateStatus(inComingCall);

        }catch (Exception ex) {
            if (inComingCall != null) {
                inComingCall.close();
            }
        }
    }
}
