package com.joyhonest.joyslipstream;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.joyhonest.joyslipstream.databinding.ActivityGridBinding;
import com.joyhonest.joyslipstream.databinding.MyGridNodeBinding;

import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class GridActivity extends AppCompatActivity  implements View.OnClickListener {
     ActivityGridBinding binding;


    private  MyAdapter myAdapter;

    private List<MyNode> nodes;

    private List<Uri> mList;

    private List<String> local_PhotolistFiles; // = JoyApp.getInstance().local_PhotoList;
    private List<String> local_VideolistFiles;

    private boolean bExit=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bExit = false;
        binding=ActivityGridBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.waitView.setVisibility(View.INVISIBLE);

        binding.btnBack.setOnClickListener(this);

        local_PhotolistFiles = new ArrayList<String>();
        local_VideolistFiles = new ArrayList<String>();

        if(JoyApp.BROW_TYPE == JoyApp.Brow_Video)
        {
            binding.imageGrid.setImageResource(R.mipmap.video_gallery_jh);
        }
        else
        {
            binding.imageGrid.setImageResource(R.mipmap.photo_gallery_jh);
        }

        binding.btnDownload.setOnClickListener(this);
        binding.btnDelete.setOnClickListener(this);
        binding.btnSelect.setOnClickListener(this);
        binding.btnBack.setOnClickListener(this);

        binding.btnOk.setOnClickListener(this);
        binding.btnCancel1.setOnClickListener(this);

        WindowManager manger = getWindowManager();
        Display display = manger.getDefaultDisplay();
        //屏幕宽度
        Point point = new Point();
        display.getSize(point);
        int screenWidth = point.x; //display.getWidth();
        binding.Gridview.setNumColumns((int) (screenWidth / Storage.dip2px(this, 100)) - 1);
//        binding.Gridview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//
//                for(MyNode node:nodes)
//                {
//                    if(node.nSelect==0)
//                    {
//                        node.nSelect = 1;
//                    }
//                }
//                MyNode node = nodes.get(position);
//                node.nSelect = 2;
//                myAdapter.notifyDataSetChanged();
//                return true;
//            }
//        });


        binding.Gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                boolean bSelect=false;
                for(MyNode node:nodes)
                {
                    if(node.nSelect!=0)
                        bSelect = true;
                    break;
                }
                if(bSelect)
                {
                    MyNode node = nodes.get(position);
                    if(node.nSelect == 2)
                    {
                        node.nSelect = 1;
                    }
                    else
                    {
                        node.nSelect = 2;
                    }
                    myAdapter.notifyDataSetChanged();
                }
                else
                {
                    JoyApp.dispList.clear();
                    if(JoyApp.BROW_TYPE == JoyApp.Brow_Photo)
                    {
                        for (MyNode nodea : nodes) {
                            JoyApp.dispList.add(nodea.sPath);
                        }
                        JoyApp.PlayBtnVoice();
                        JoyApp.dispListInx = position;
                        Intent mainIntent = new Intent(GridActivity.this, DispPhotoActivity.class);
                        startActivity(mainIntent);
                        overridePendingTransition(0, 0);
                    }
                    else
                    {
                        MyNode node = nodes.get(position);
                        JoyApp.dispList.add(node.sPath);
                        startActivity(new Intent(GridActivity.this, DispVideoActivity.class));
                        overridePendingTransition(0, 0);
                    }
                }

            }
        });

        bExit = false;
        _Init_Theard init = new _Init_Theard();
        init.start();

      //  EventBus.getDefault().register(this);

        binding.DeleteAlertView.setVisibility(View.GONE);

        EventBus.getDefault().register(this);

    }



    private  void SetEditMode()
    {
                boolean bIsSelectMode = false;
                for(MyNode node:nodes)
                {
                    if(node.nSelect!=0)
                    {
                        bIsSelectMode=true;
                        break;

                    }
                }
                if(!bIsSelectMode)
                {
                    for(MyNode node:nodes)
                    {
                        node.nSelect = 1;
                    }
                    binding.btnSelect.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this,R.color.cyan)));
                }
                else
                {
                    for(MyNode node:nodes)
                    {
                        node.nSelect = 0;
                    }
                    binding.btnSelect.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this,R.color.white)));
                }
//                MyNode node = nodes.get(position);
//                node.nSelect = 2;
                myAdapter.notifyDataSetChanged();

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
        super.onDestroy();
        bExit = true;
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        JoyApp.F_MakeFullScreen(this);
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
        JoyApp.PlayBtnVoice();
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.btn_back)
        {
            bExit = true;
            onBackPressed();
            overridePendingTransition(0, 0);
        }
        if(id == R.id.btn_delete)
        {
            JoyApp.PlayBtnVoice();
            binding.DeleteAlertView.setVisibility(View.VISIBLE);

        }
        if(id == R.id.btn_cancel1)
        {
            JoyApp.PlayBtnVoice();
            for(MyNode node:nodes)
            {
                node.nSelect = 0;
            }
            myAdapter.notifyDataSetChanged();
            binding.btnSelect.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this,R.color.white)));
            binding.DeleteAlertView.setVisibility(View.GONE);
        }

        if(id == R.id.btn_ok)
        {
            JoyApp.PlayBtnVoice();
            binding.btnSelect.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this,R.color.white)));
            F_DeleteSelectedFiles_A();
        }
        if (id==R.id.btn_select){
            JoyApp.PlayBtnVoice();
            SetEditMode();
        }


    }
    private class _Init_Theard extends Thread {
        @Override
        public void run() {
            F_Init();
        }
    }



    private String getFileName(String pathandname) {

        int start = pathandname.lastIndexOf("/");
        int end = pathandname.lastIndexOf(".");
        if (start != -1 && end != -1) {
            return pathandname.substring(start + 1);
        } else {
            return null;
        }

    }

    private void F_SaveBitmap(Bitmap bm, String sPath) {
        Log.e("WIFI", "保存图片");
        if (bm == null)
            return;
        File f = new File(sPath);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            Log.i("WIFI", "已经保存");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private Bitmap getVideoThumbnail(Uri uri) {

        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(this,uri);
            bitmap = retriever.getFrameAtTime(1);
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, 100, 100);

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    private Bitmap GetSuonuitu(Uri uri)
    {

        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor,null,options);

            options.inJustDecodeBounds = false;
            //计算缩放比
            int be = (int) (options.outHeight / (float) 100);
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



    }

    private Bitmap getVideoThumbnail(String filePath) {
        Bitmap bitmap = null;
        String strs = "";
        String str = getFileName(filePath);
        strs = this.getCacheDir() + "/" + str + ".v_thb.mp4";
        bitmap = BitmapFactory.decodeFile(strs);
        if (bitmap != null)
            return bitmap;


        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime(1);
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, 100, 100);

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }

        F_SaveBitmap(bitmap, strs);
        return bitmap;
    }

    private void F_DispData()
    {
        int nBrow = JoyApp.BROW_TYPE;
        if(nBrow == JoyApp.Brow_Photo)
        {

            mList = JoyApp.mList;
            if(nodes!=null)
            {
                nodes.clear();
            }
            nodes = new ArrayList<MyNode>();
            if ( mList!= null) {
                for(Uri uri : mList)
                {
                    if(bExit)
                        break;
                    {
                        Bitmap bitmap = GetSuonuitu(uri);
                        MyNode node = new MyNode(nBrow);
                        node.bitmap = bitmap;
                        node.sPath = uri.toString();
                        nodes.add(node);
                    }
                }
            }
        }
        else
        {
            mList = JoyApp.mList;
            if(nodes!=null)
            {
                nodes.clear();
            }
            nodes = new ArrayList<MyNode>();
            if ( mList!= null) {
                //for (String sPath : mList)
                for(Uri uri : mList)
                {
                    if(bExit)
                        break;;
                    {
                        Bitmap bitmap = getVideoThumbnail(uri);
                        MyNode node = new MyNode(0);
                        node.bitmap = bitmap;
                        node.sPath = uri.toString();
                        nodes.add(node);
                    }
                }
            }
        }

        if(bExit) {
            if(nodes!=null)
            {
                nodes.clear();
            }
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                myAdapter = (MyAdapter) new MyAdapter(GridActivity.this, R.layout.my_grid_node, nodes);
                binding.Gridview.setAdapter(myAdapter);
                myAdapter.notifyDataSetChanged();
                binding.waitView.setVisibility(View.INVISIBLE);
            }
        });


    }

    private  void F_Init()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.waitView.setVisibility(View.VISIBLE);
            }
        });

        F_DispData();



    }


    boolean bDeletting = false;
    private final ReentrantLock lock = new ReentrantLock();

    private String F_GetTmpFileName(String sPath) {
        String strs = "";
        String sf = getFileName(sPath);
        strs = this.getCacheDir() + "/" + sf + ".thb.png";
        return strs;
    }

    private void F_DeleteSelectedFiles_A() {

        bDeletting = true;
        {
            Iterator<MyNode> it = nodes.iterator();
            while (it.hasNext()) {
                if (bExit) {
                    bExit = false;
                    break;
                }
                MyNode node = it.next();
                if (node.nSelect == 2) {
                    String sPath = node.sPath;
                    String str_tmp = F_GetTmpFileName(sPath);
                    try {
                        JoyApp.DeleteImage(sPath);
                        File file1 = new File(str_tmp);
                        if(file1.exists() && file1.isFile())
                            file1.delete();
                    } catch (Exception e) {
                        ;
                    }
                    finally {
                        Uri uri = Uri.parse(sPath);
                        mList.remove(uri);
                    }
                }
            }

            lock.lock();
            try {
                Iterator<MyNode> ita = nodes.iterator();
                while (ita.hasNext()) {
                    MyNode node = ita.next();
                    if (node.nSelect == 2) {
                        {
                            ita.remove();
                        }
                    }
                }
            } finally {
                lock.unlock();
            }
            EventBus.getDefault().post("", "Delete_OK");
            bDeletting = false;
        }
    }

    @Subscriber(tag = "Delete_OK")
    private void Delete_OK(String str) {

        binding.DeleteAlertView.setVisibility(View.GONE);
        for(MyNode node:nodes)
        {
            node.nSelect = 0;
        }
        myAdapter.notifyDataSetChanged();

    }


    //自定义适配器
    class MyAdapter extends BaseAdapter {
        //上下文对象
        private Context context;
        private int viewResourceId;
        private List<MyNode> mfilelist;
        private LayoutInflater mInflater;


        MyAdapter(Context context, int resourceid, List<MyNode> objects) {
            this.context = context;
            this.viewResourceId = resourceid;
            this.mfilelist = objects;
            this.mInflater = LayoutInflater.from(context);
//            if(JoyApp.bScreenRota)
//            {
//                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
//            }
//            else
//            {
//
//                /*
//            if(JoyApp.bRot)
//                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
//            else
//            */
//                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//            }
        }

        public int getCount() {
            return mfilelist.size();
        }

        public Object getItem(int item) {
            return item;
        }

        public long getItemId(int id) {
            return id;
        }

        //创建View方法
        public View getView(int position, View convertView, ViewGroup parent) {
            MyViewHolder holder = null;
            MyNode node = mfilelist.get(position);
            if (convertView == null) {

                MyGridNodeBinding binding;//
                binding = MyGridNodeBinding.inflate(mInflater);
                convertView = binding.getRoot();
                //convertView = mInflater.inflate(viewResourceId, null);
                holder = new MyViewHolder();
                holder.progressBar = binding.GridProgressBar1;
                holder.progressBar.setProgress(0);
                holder.progressBar.setMax(1000);
                holder.icon = binding.GridImageView1;
                holder.video_bg = binding.videoBg;
                holder.caption = binding.GridTextView1;
                holder.sUrl = "";
                holder.SelectImg = binding.selectIcon;
//                holder.progressBar = (ProgressBar) convertView.findViewById(R.id.Grid_progressBar1);
//                holder.progressBar.setProgress(0);
//                holder.progressBar.setMax(1000);
//                holder.icon = (ImageView) convertView.findViewById(R.id.Grid_imageView1);
//                holder.video_bg = (ImageView) convertView.findViewById(R.id.video_bg);
//                holder.caption = (TextView) convertView.findViewById(R.id.Grid_textView1);
//                holder.sUrl = "";
//                holder.SelectImg = convertView.findViewById(R.id.select_icon);
                convertView.setTag(holder);
            } else {
                holder = (MyViewHolder) convertView.getTag();
            }
            //if(holder.sUrl!="")
            if (node != null) {
                {
                    if (node.bitmap != null) {
                        holder.icon.setImageBitmap(node.bitmap);
                    }
                    else {
                        // Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
                        //holder.icon.setImageBitmap(bmp);
                    }
                }
                if (node.nType == MyNode.TYPE_Remote) {
                    holder.progressBar.setVisibility(View.VISIBLE);
                    if (node.nStatus == MyNode.Status_downloaded) {
                        holder.progressBar.setProgress(1000);
                        holder.caption.setVisibility(View.INVISIBLE);
                    } else if (node.nStatus == MyNode.Status_downloading) {
                        holder.progressBar.setProgress((int) node.nPre);
                        holder.caption.setTextColor(0xFF0000FF);
                        holder.caption.setVisibility(View.VISIBLE);
                        if (node.nPre < 10)
                            node.nPre = 10;
                        holder.caption.setText(getString(R.string.downloading) + " " + node.nPre / 10 + "%");
                    } else {
                        holder.progressBar.setProgress(0);
                        holder.caption.setVisibility(View.INVISIBLE);
                    }
                } else {
                    holder.progressBar.setVisibility(View.INVISIBLE);
                    holder.caption.setVisibility(View.INVISIBLE);
                }
            }



            if(node.nSelect == 1)
            {
                holder.SelectImg.setVisibility(View.VISIBLE);
                holder.SelectImg.setImageResource(R.mipmap.noselected_icon);
            }
            else if(node.nSelect == 2)
            {
                holder.SelectImg.setVisibility(View.VISIBLE);
                holder.SelectImg.setImageResource(R.mipmap.selected_icon);
            }
            else
            {
                holder.SelectImg.setVisibility(View.INVISIBLE);
            }

            if (JoyApp.BROW_TYPE == JoyApp.Brow_Video){
                holder.video_bg.setVisibility(View.VISIBLE);
            }
            return convertView;
        }

        public class MyViewHolder {
            ProgressBar progressBar;
            TextView caption;
            ImageView icon;
            ImageView video_bg;
            ImageView SelectImg;
            String sUrl;
        }
    }





    @Subscriber(tag = "Go2Background")
    private void Go2Background(String str) {
        if(!JoyApp.bGotsystemActivity) {
            finish();

            overridePendingTransition(0, 0);
        }
    }




}