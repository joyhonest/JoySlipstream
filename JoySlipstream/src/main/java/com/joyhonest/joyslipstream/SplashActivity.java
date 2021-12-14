package com.joyhonest.joyslipstream;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;

public class SplashActivity extends AppCompatActivity {

    private final int SPLASH_DISPLAY_LENGHT = 1500;
    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        JoyApp.F_MakeFullScreen(this);
        handler = new Handler();
        // 延迟SPLASH_DISPLAY_LENGHT时间然后跳转到MainActivity
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, FirstActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
            }
        }, SPLASH_DISPLAY_LENGHT);
        EventBus.getDefault().register(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    @Override
    protected void onStop() {
        super.onStop();
        JoyApp.activityAount--;
        if (JoyApp.activityAount == 0) {
            if (!JoyApp.bGotsystemActivity) {
                EventBus.getDefault().post("", "Go2Background");
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        JoyApp.bGotsystemActivity = false;
        JoyApp.activityAount++;
    }

    @Override
    protected void onResume() {
        super.onResume();
        JoyApp.F_MakeFullScreen(this);
    }
    @Subscriber(tag = "Go2Background")
    private void Go2Background(String str) {
        if(!JoyApp.bGotsystemActivity) {
            finish();

            overridePendingTransition(0, 0);
        }
    }
}