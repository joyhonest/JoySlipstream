package com.joyhonest.joyslipstream;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;

public class HelpActivity extends AppCompatActivity {
Button back;
Button next;
ConstraintLayout imageView;
int mode=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        back=findViewById(R.id.back_help);
        next=findViewById(R.id.swipe_next_help);
        imageView=findViewById(R.id.constraintLayout_help);


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode==1){
                    imageView.setBackgroundResource(R.mipmap.help_info3_jh);
                }
                if (mode==0) {
                    imageView.setBackgroundResource(R.mipmap.help_info2_jh);
                    mode=1;
                }


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