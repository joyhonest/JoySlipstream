package com.joyhonest.joyslipstream;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.joyhonest.joyslipstream.databinding.ActivityGalleryBinding;

import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GalleryActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityGalleryBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGalleryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backGallery.setOnClickListener(this);
        binding.galleryPhoto.setOnClickListener(this);
        binding.galleryVideo.setOnClickListener(this);
        if(JoyApp.mList!=null)
        {
            JoyApp.mList.clear();
            JoyApp.mList = null;
        }


        _Init_Theard init = new _Init_Theard();
        init.start();

        EventBus.getDefault().register(this);


    }

    @Override
    public void onClick(View v) {
        JoyApp.PlayBtnVoice();
        if(v == binding.backGallery)
        {

            onBackPressed();
        }
        if(v == binding.galleryPhoto)
        {
            JoyApp.BROW_TYPE=JoyApp.Brow_Photo;
            JoyApp.mList = mListPhoto;
            startActivity(new Intent(GalleryActivity.this, GridActivity.class));
            overridePendingTransition(0,0);
        }

        if(v == binding.galleryVideo)
        {
            JoyApp.BROW_TYPE=JoyApp.Brow_Video;
            JoyApp.mList = mListVideo;
            startActivity(new Intent(GalleryActivity.this, GridActivity.class));
            overridePendingTransition(0,0);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        bExit = true;
    }

    private List<MyNode> nodes;
    //private List<String> mList;
    private List<Uri> mList;
    private List<Uri> mListPhoto;
    private List<Uri> mListVideo;


    private boolean bExit = false;

    private class _Init_Theard extends Thread {
        @Override
        public void run() {
            F_Init();
        }
    }


    private void F_Init()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.waitView.setVisibility(View.VISIBLE);
            }
        });

        final int nBrow = JoyApp.BROW_TYPE;
        bExit = false;
        F_GetAllPhotoLocal();
        F_GetAllVideoLocal();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.waitView.setVisibility(View.INVISIBLE);
                F_DispFileCount();
            }
        });

        //F_DispData();
    }


    private void  F_DispFileCount()
    {
        if(mListPhoto!=null) {
            String s = "" + mListPhoto.size();
            binding.photoCount.setText(s);
        }
        else
        {
            binding.photoCount.setText("0");
        }

        if(mListVideo!=null) {
            String s = "" + mListVideo.size();
            binding.videoCount.setText(s);
        }
        else
        {
            binding.videoCount.setText("0");
        }
    }

    class MapComparator implements Comparator<Uri> {
        public int compare(Uri lhs, Uri rhs) {
            return rhs.compareTo(lhs);
        }
    }

    private void F_GetAllPhotoLocal() {
        List<Uri> list = JoyApp.F_GetAllLocalFiles(true);
        mListPhoto = list;
        Collections.sort(mListPhoto, new MapComparator());
    }
    private void F_GetAllVideoLocal() {
        List<Uri> list = JoyApp.F_GetAllLocalFiles(false);
        mListVideo = list;
        Collections.sort(mListVideo, new MapComparator());
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
        JoyApp.activityAount++;
        JoyApp.bGotsystemActivity = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        JoyApp.F_MakeFullScreen(this);
        F_DispFileCount();
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