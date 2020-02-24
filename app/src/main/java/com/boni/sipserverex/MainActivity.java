package com.boni.sipserverex;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.text.ParseException;

// 참고) https://developer.android.com/guide/topics/connectivity/sip#java
public class MainActivity extends AppCompatActivity {
    private Button connectProfileButton, makeAudioCallButton;

    public SipManager sipManager = null;
    private SipProfile sipProfile = null;
    private SipProfile.Builder builder = null;
    private SipAudioCall.Listener listener = null;
    public SipAudioCall call = null;
    public IncomingCallReceiver callReceiver;
    private Intent intent;

    private String userName;
    private String domain;
    private String password;
    private String sipAddress;
    private String INTENT_ACTION = "android.SipDemo.INCOMING_CALL";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById();

        init();

        events();
    }

    private void findViewById() {
        connectProfileButton = findViewById(R.id.connectProfileButton);
        makeAudioCallButton = findViewById(R.id.makeAudioCallButton);
    }

    private void init() {
        // sipManager 인스턴스 생성
        if (sipManager == null) {
            sipManager = SipManager.newInstance(this);
        }
        intent = new Intent();

        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_ACTION);

        callReceiver = new IncomingCallReceiver();
        this.registerReceiver(callReceiver, filter);
    }

    /**
     * 각종 이벤트
     */
    private void events() {
        connectProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                buildSIPProfile();

                openSIPManager();

            }
        });

        makeAudioCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    call = sipManager.makeAudioCall(sipProfile.getUriString(), sipAddress, listener, 30);
                } catch (SipException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            sipManager.setRegistrationListener(sipProfile.getUriString(), new SipRegistrationListener() {
                @Override
                public void onRegistering(String localProfileUri) {
                    Log.d("Event", "Registering with SIP Server...");
                }

                @Override
                public void onRegistrationDone(String localProfileUri, long expiryTime) {
                    Log.d("Event", "Ready");
                }

                @Override
                public void onRegistrationFailed(String localProfileUri, int errorCode,
                                                 String errorMessage) {
                    Log.d("Event", "Registration failed.  Please check settings.");
                }
            });
        } catch (SipException e) {
            e.printStackTrace();

            Toast.makeText(
                    this,
                    "SipManager Listener error\n" + e.getMessage(),
                    Toast.LENGTH_SHORT
            ).show();
        }

        // 오디오 Call 이벤트
        listener = new SipAudioCall.Listener() {
            @Override
            public void onCallEstablished(SipAudioCall call) {
                super.onCallEstablished(call);
                call.startAudio();
                call.setSpeakerMode(true);
                call.toggleMute();
            }

            @Override
            public void onCallEnded(SipAudioCall call) {
                super.onCallEnded(call);
                // 전화 끝났을 때 처리 해주는 시점.
            }
        };
    }

    /**
     * 애플리케이션을 실행하는 기기의 SIP 계정과 연결된 프로필을 로컬 프로필이라고 합니다.
     * 세션이 연결된 프로필을 피어 프로필이라고 합니다.
     * SipProfile 을 사용하여 SIP 서버에 로그인하면 SIP 통화를 발신할 위치로 기기가 SIP 주소에 효과적으로 등록됩니다.
     */
    private void buildSIPProfile() {
        //  SipProfile 을 생성하여 SIP 서버에 등록하고 등록 이벤트를 추적함
        try {
            // 연결할 사용자와 도메인, 비밀번호로 프로필 생성
            builder = new SipProfile.Builder(userName, domain);
            builder.setPassword(password);
            sipProfile = builder.build();

        } catch (ParseException e) {
            e.printStackTrace();

            Toast.makeText(
                    this,
                    "SipProfile build error \n" + e.getMessage(),
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    /**
     * 전화 걸기 또는 일반 SIP 전화 받기를 위한 로컬 프로필 열기
     * 발신자는 mSipManager.makeAudioCall 발신할 수 있음.
     * 'android.SipDemo.INCOMING_CALL' 필터를 통해 이벤트 받는다.
     * <p>
     * PendingIntent는?
     * Intent를 가지고 있는 클래스로,
     * 기본 목적은 다른 애플리케이션(다른 프로세스)의 권한을 허가하여
     * 가지고 있는 Intent를 마치 본인 앱의 프로세스에서 실행하는 것처럼 사용하게 하는 것입니다.
     * 참고 : https://www.charlezz.com/?p=861
     */
    private void openSIPManager() {
        if (sipProfile != null) {
            // 이벤트 받을 필터 설정
            intent.setAction(INTENT_ACTION);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, Intent.FILL_IN_DATA);
            try {
                sipManager.open(sipProfile, pendingIntent, null);
            } catch (SipException e) {
                e.printStackTrace();
                Toast.makeText(
                        this,
                        "SipManager open error\n" + e.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    /**
     * 프로필 사용이 완료 되면 닫아 줘야 함.
     * <p>
     * (아직 어느 시점에 닫아 줘야할지 모르겠다...아마 등록이 완료 된 시점 아닐까 싶다.)
     */
    public void closeLocalProfile() {
        if (sipManager == null) {
            return;
        } else {
            try {
                if (sipProfile != null) {
                    sipManager.close(sipProfile.getUriString());
                }
            } catch (SipException e) {
                e.printStackTrace();

                Toast.makeText(
                        this,
                        "Close SIP Profile error\n" + e.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    /**
     * ...??
     *
     * @param inComingCall
     */
    public void updateStatus(SipAudioCall inComingCall) {

    }
}