package com.boni.sipserverex;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.os.Bundle;
import android.widget.Toast;

import java.text.ParseException;

public class MainActivity extends AppCompatActivity {

    public SipManager sipManager = null;
    private SipProfile sipProfile = null;
    private Intent intent;

    private String userName;
    private String domain;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // sipManager 인스턴스 생성
        if (sipManager == null) {
            sipManager = SipManager.newInstance(this);
        }

        /*
         * 애플리케이션을 실행하는 기기의 SIP 계정과 연결된 프로필을 로컬 프로필이라고 합니다.
         * 세션이 연결된 프로필을 피어 프로필이라고 합니다.
         * SipProfile 을 사용하여 SIP 서버에 로그인하면 SIP 통화를 발신할 위치로 기기가 SIP 주소에 효과적으로 등록됩니다.
         */
        //  SipProfile 을 생성하여 SIP 서버에 등록하고 등록 이벤트를 추적함
        SipProfile.Builder builder = null;
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
            return;
        }

        if (sipProfile != null) {
            /*
             * 전화 걸기 또는 일반 SIP 전화 받기를 위한 로컬 프로필 열기
             * 발신자는 mSipManager.makeAudioCall 발신할 수 있음.
             * 'android.SipDemo.INCOMING_CALL' 필터를 통해 이벤트 받는다.
             *
             * PendingIntent는?
             * Intent를 가지고 있는 클래스로,
             * 기본 목적은 다른 애플리케이션(다른 프로세스)의 권한을 허가하여
             * 가지고 있는 Intent를 마치 본인 앱의 프로세스에서 실행하는 것처럼 사용하게 하는 것입니다.
             * 참고 : https://www.charlezz.com/?p=861
             * */
            intent = new Intent();
            // 이벤트 받을 필터 설정
            intent.setAction("android.SipDemo.INCOMING_CALL");
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
                return;
            }

        }
    }
}