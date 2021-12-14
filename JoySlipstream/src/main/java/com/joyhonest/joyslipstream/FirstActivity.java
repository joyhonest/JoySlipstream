package com.joyhonest.joyslipstream;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;

public class FirstActivity extends AppCompatActivity {
Button start;
Button help;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        start=findViewById(R.id.start_main);
        help=findViewById(R.id.help_info);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JoyApp.PlayBtnVoice();
                startActivity(new Intent(FirstActivity.this,PlayActivity.class));
                overridePendingTransition(0,0);
            }
        });

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JoyApp.PlayBtnVoice();
                startActivity(new Intent(FirstActivity.this,HelpActivity.class));
                overridePendingTransition(0,0);
            }
        });
        EventBus.getDefault().register(this);
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
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscriber(tag = "Go2Background")
    private void Go2Background(String str) {
        if(!JoyApp.bGotsystemActivity) {
            finish();

            overridePendingTransition(0, 0);
        }
    }
}