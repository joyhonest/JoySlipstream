package com.joyhonest.joyslipstream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.joyhonest.joyslipstream.databinding.ActivityDispPhotoBinding;

import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;

import java.io.FileDescriptor;
import java.util.List;

public class DispPhotoActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener{


    private ActivityDispPhotoBinding binding;
    private List<String> nodes;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_disp_photo);
        binding = ActivityDispPhotoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        JoyApp.F_MakeFullScreen(this);

        binding.indexView.setText((JoyApp.dispListInx+1)+"/"+JoyApp.dispList.size());
        nodes = JoyApp.dispList;

        binding.photoVp.setAdapter(adapter);

        binding.photoVp.addOnPageChangeListener(this);
        binding.photoVp.setCurrentItem(JoyApp.dispListInx);

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JoyApp.PlayBtnVoice();
                onBackPressed();
            }
        });


        EventBus.getDefault().register(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        JoyApp.bGotsystemActivity = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
//        if(!JoyApp.bNormalExit)
//            EventBus.getDefault().post("a","GotoExit");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //     JoyApp.bNormalExit = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //   JoyApp.bNormalExit = false;
        JoyApp.F_MakeFullScreen(this);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override

    public void onPageScrollStateChanged(int arg0) {


    }


    @Override

    public void onPageScrolled(int arg0, float arg1, int arg2) {


    }


    @Override

    public void onPageSelected(int arg0) {
        //fileInx_label.setText("" + (arg0 + 1) + "/" + nodes.size());
        binding.indexView.setText((arg0+1)+"/"+JoyApp.dispList.size());
    }

    private PagerAdapter adapter = new PagerAdapter() {
        @Override
        public int getCount() {
//                            return imagePathArray.size();
            return nodes.size();
            //return 4;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        /**
         * 销毁当前page的相隔2个及2个以上的item时调用
         */
        @Override
        public void destroyItem(ViewGroup container, int position, Object o) {
            container.removeView((View) o);
        }

        //设置ViewPager指定位置要显示的view
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final String strPath = nodes.get(position);

            MyImageView im = new MyImageView(DispPhotoActivity.this);
            im.bCanScal = true;
            im.setMaxZoom(3.5f);
            im.setScaleType(ImageView.ScaleType.FIT_XY);
            im.setImageDrawable(new BitmapDrawable(getResources(), LoadBitmap(strPath)));
            container.addView(im);
            return im;
        }


    };

    private Bitmap LoadBitmap(String sPath) {

        Uri uri = Uri.parse(sPath);

        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor,null,options);

            options.inJustDecodeBounds = false;
            //计算缩放比
            int be = (int) (options.outHeight / (float) 1280);
            if (be <= 0)
                be = 1;
            options.inSampleSize = be;
            //重新读入图片，注意这次要把options.inJustDecodeBounds 设为 false哦
            bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor,null,options);
            parcelFileDescriptor.close();
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        // 获取这个图片的宽和高
//        Bitmap bitmap = BitmapFactory.decodeFile(sPath, options); //此时返回bm为空
//        options.inJustDecodeBounds = false;
//        //计算缩放比
//        int be = (int) (options.outHeight / (float) 720);
//        if (be <= 0)
//            be = 1;
//        options.inSampleSize = be;
//        //重新读入图片，注意这次要把options.inJustDecodeBounds 设为 false哦
//        bitmap = BitmapFactory.decodeFile(sPath, options);
//        return bitmap;
    }

    @Subscriber(tag = "GotoExit_joy")
    private  void GotoExit_joy(String str)
    {
        finish();
        overridePendingTransition(0, 0);
    }

    @Subscriber(tag = "Go2Background")
    private void Go2Background(String str) {
        if(!JoyApp.bGotsystemActivity) {
            finish();

            overridePendingTransition(0, 0);
        }
    }


}
