package com.joyhonest.slipstream_wifi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.joyhonest.joyslipstream.PlayActivity;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.start).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, PlayActivity.class));
            overridePendingTransition(0,0);
        });

    }
}