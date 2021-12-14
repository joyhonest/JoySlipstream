package com.joyhonest.joyslipstream;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import androidx.appcompat.app.AppCompatActivity;
import com.joyhonest.joyslipstream.databinding.ActivityDispVideoJhBinding;
import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;

public class DispVideoActivity extends AppCompatActivity {
    ActivityDispVideoJhBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDispVideoJhBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        JoyApp.F_MakeFullScreen(this);
        String strPath = JoyApp.dispList.get(0);
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JoyApp.PlayBtnVoice();
                onBackPressed();
            }
        });

        MediaController mediaController = new MediaController(this);
        binding.videoPlay.setVisibility(View.VISIBLE);
        binding.videoPlay.setMediaController(mediaController);
        mediaController.setMediaPlayer(binding.videoPlay);
        Uri uri = Uri.parse(strPath);
        binding.videoPlay.setVideoURI(uri);
        binding.videoPlay.start();
        binding.videoPlay.requestFocus();
        EventBus.getDefault().register(this);
    }

    @Subscriber(tag="PlayComplete")
    private void PlayComplete(String str)
    {
        onBackPressed();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    private  void F_StopPlay()
    {
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
        //   JoyApp.bNormalExit = false;
        JoyApp.F_MakeFullScreen(this);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        binding.videoPlay.stopPlayback();
        overridePendingTransition(0,0);
    }

    @Subscriber(tag = "Go2Background")
    private void Go2Background(String str) {
        if(!JoyApp.bGotsystemActivity)
        {
            binding.videoPlay.stopPlayback();
            finish();
            overridePendingTransition(0, 0);
        }
    }
}
