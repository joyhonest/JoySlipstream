package com.joyhonest.joyslipstream;

import android.app.Activity;
import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

import org.simple.eventbus.EventBus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class JoyApp  //extends Application implements Application.ActivityLifecycleCallbacks
{
    static int activityAount = 0;
    public static boolean bGotsystemActivity = false;



    public static  boolean   bHSpeed = false;
    public static  boolean   bCorrection_adj = false;

    public static  boolean   bUp=false;
    public static  boolean   bDn=false;
    public static  boolean   bStop = false;
    public static  boolean   bCompass = false;


    public static String sGallery = "";
    private static Context singleton = null;
    public static  boolean   bPath = false;

    //本地图片和视频存储路径
    public static String sLocalPath = "";
    public static String sSDPath = "";
    public static boolean bIsConnect = false;
//    public static  boolean  bNormalExit = false;
    public static int nOrg;


    public  static List<Uri> mList;


    public static boolean bFlyDisableAll;


    public static boolean bConnected;

    public static boolean bRecording;

    public static SoundPool soundPool;
    private static int music_photo = -1;
    private static int music_btn = -1;

    //判断查看的是图片还是视频
    public static final int Brow_Photo = 0;
    public static final int Brow_Video = 1;
    public static int BROW_TYPE = Brow_Photo;

    public static List<String> dispList=new ArrayList<>();
    public static int dispListInx=0;

//    @Override
//    public void onCreate() {
//        super.onCreate();
//        singleton = this;
//        registerActivityLifecycleCallbacks(this);
//
//
//        if (Build.VERSION.SDK_INT >= 21) {  //androdi 5.0 以上
//            SoundPool.Builder builder = new SoundPool.Builder();
//            builder.setMaxStreams(2);//传入音频数量
//            AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
//            attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);//设置音频流的合适的属性
//            builder.setAudioAttributes(attrBuilder.build());//加载一个AudioAttributes
//            soundPool = builder.build();
//        } else {
//            soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
//        }
//
//        music_photo = soundPool.load(singleton, R.raw.photo_m_joy, 1);
//        music_btn = soundPool.load(singleton, R.raw.button46_joy, 1);
//
//
//    }




    public static void initJoySlipstream(Context context)
    {
        singleton = context.getApplicationContext();
        if (Build.VERSION.SDK_INT >= 21) {  //androdi 5.0 以上
            SoundPool.Builder builder = new SoundPool.Builder();
            builder.setMaxStreams(2);//传入音频数量
            AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
            attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);//设置音频流的合适的属性
            builder.setAudioAttributes(attrBuilder.build());//加载一个AudioAttributes
            soundPool = builder.build();
        } else {
            soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        }

        music_photo = soundPool.load(singleton, R.raw.photo_m_joy, 1);
        music_btn = soundPool.load(singleton, R.raw.button46_joy, 1);
    }

    //播放点击按钮的音效
    public static void PlayBtnVoice() {

        if (music_btn != -1)
            soundPool.play(music_btn, 1, 1, 0, 0, 1);
    }

    //显示图片的音效
    public static void PlayPhotoMusic() {

        if (music_photo != -1)
            soundPool.play(music_photo, 1, 1, 0, 0, 1);

    }


    public static String getFileNameFromDate(boolean bVideo) {
        Date d = new Date();
        SimpleDateFormat f = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.getDefault());
        String strDate = f.format(d);
        String StroragePath = "";

        StroragePath = sLocalPath;
//        if(!blocal)
//            StroragePath = sSDPath;
        String ext = "mp4";
        if (!bVideo) {
            ext = "png";
        }
        String recDir = StroragePath;
        File dirPath = new File(recDir);
        if (!dirPath.exists()) {
            dirPath.mkdirs();
        }
        String recPath = String.format("%s/%s.%s", recDir, strDate, ext);
        return recPath;
    }

    //获取是否存在NavigationBar
    public static void F_MakeFullScreen(Context context) {


        Activity activity = (Activity) context;

        Window window = activity.getWindow();

        //取消状态栏
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT < 19) { // lower api
            View v = window.getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else {
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            //for new api versions.
            View decorView = window.getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }

    }


    public static void F_GetOrg(Activity activity) {
        // int sf = activity.getWindowManager().getDefaultDisplay().getRotation();
        int sf = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            sf = activity.getDisplay().getRotation();
        }
        if (sf == Surface.ROTATION_90) {
            nOrg = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }
        if (sf == Surface.ROTATION_270) {
            nOrg = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
        }
    }


    //sGallery


    public static boolean isAndroidQ() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q);
    }


    public static void DeleteImage(String imgPath) {

        try {
        Uri uri = Uri.parse(imgPath);
        Context mContext = singleton.getApplicationContext();
        ContentResolver resolver = mContext.getContentResolver();
        int count = resolver.delete(uri, null, null);
        } catch (Exception ignored) {

        }
    }


    public static List<Uri> F_GetAllLocalFiles(boolean bPicture) {
        String sPath = sGallery;

        List list = new ArrayList<Uri>();
        Context mContext = singleton.getApplicationContext();
        ContentResolver resolver = mContext.getContentResolver();
        if (!sPath.endsWith("/")) {
            sPath += "/";
        }
        Cursor cursor;
        Uri contentUri;
        if (bPicture) {
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            if (isAndroidQ()) {
                cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.RELATIVE_PATH + "=?",
                        new String[]{Environment.DIRECTORY_DCIM + File.separator + sPath}, null);
            } else {
//                cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + " like ?",
//                        new String[]{"%"+Environment.DIRECTORY_DCIM + File.separator + sPath+"%"}, null);
                cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + " like ?",
                        new String[]{sLocalPath + "%"}, null);
            }

        } else {
            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            if (isAndroidQ()) {
                cursor = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Video.Media._ID}, MediaStore.Video.Media.RELATIVE_PATH + "=?",
                        new String[]{Environment.DIRECTORY_DCIM + File.separator + sPath}, null);
            } else {
                cursor = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Video.Media._ID}, MediaStore.Video.Media.DATA + " like ?",
                        new String[]{sLocalPath + "%"}, null);
            }
        }

        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(0);
                Uri uri = ContentUris.withAppendedId(contentUri, id);
                list.add(uri);
            }
        }
        return list;
    }


    public static void F_CreateLocalDir(String str) {
        boolean mRemote = false;
        String StroragePath = "";
        String sVendor = "";
        String sVendor_SD = null;
        sVendor = str;
        sGallery = str;
        if (isAndroidQ()) {
            File file = singleton.getExternalFilesDir(sVendor);
            if (file != null) {
                sLocalPath = file.getAbsolutePath();
            }

        } else {
            try {
                StroragePath = Environment.getExternalStorageDirectory().getPath();
            } catch (Exception e) {
                return;
            }
            String recDir;
            File fdir;
            recDir = String.format("%s/%s", StroragePath, sVendor);
            fdir = new File(recDir);
            boolean re = false;
            if (!fdir.exists()) {
                re = fdir.mkdirs();
            }
            sLocalPath = recDir;
        }

    }


    public static boolean F_CheckIsExit(String slocal, String sfile, boolean bPhoto) {
        if (singleton == null)
            return false;

        ContentResolver resolver = singleton.getContentResolver();
        Cursor cursor = null;

        if (!slocal.endsWith("/")) {
            slocal += "/";
        }

        Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        if (!bPhoto) {
            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }

        if (bPhoto) {
            cursor = resolver.query(contentUri, new String[]{MediaStore.Images.Media._ID},
                    MediaStore.Images.Media.RELATIVE_PATH + "=? and " + MediaStore.Images.Media.DISPLAY_NAME + "=?",
                    new String[]{slocal, sfile}, null);

        } else {
            cursor = resolver.query(contentUri, new String[]{MediaStore.Video.Media._ID},
                    MediaStore.Video.Media.RELATIVE_PATH + "=? and " + MediaStore.Video.Media.DISPLAY_NAME + "=?",
                    new String[]{slocal, sfile}, null);

        }
        if (cursor != null) {
            int a = cursor.getCount();
            cursor.close();
            return a > 0;
        }
        return false;
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static void F_Save2ToGallery(String filename, boolean bPhoto) {
        if (singleton == null)
            return;

        if (isAndroidQ()) {
            File file1 = new File(filename);
            if (!file1.exists()) {
                return;
            }

            //String sVedor = sGallery;

            if (sGallery == null || sGallery.length() == 0) {
                return;
            }

            String slocal = "";

            String sfile = filename.substring(filename.lastIndexOf("/") + 1);
            String stype = filename.substring(filename.lastIndexOf(".") + 1);
            ContentResolver contentResolver = singleton.getContentResolver();
            ContentValues values = new ContentValues();
            slocal = Environment.DIRECTORY_DCIM + File.separator + sGallery;
            Uri uri = null;
            if (F_CheckIsExit(slocal, sfile, bPhoto))
                return;
            if (bPhoto) {
                values.put(MediaStore.Images.Media.DISPLAY_NAME, sfile);
                if (stype.equalsIgnoreCase("png")) {
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                } else {
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                }
                values.put(MediaStore.Images.Media.RELATIVE_PATH, slocal);
                uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values);
            } else {
                //File file = new File(filename);
                values.put(MediaStore.Video.Media.DISPLAY_NAME, sfile);
                values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
                values.put(MediaStore.Video.Media.RELATIVE_PATH, slocal);
                uri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        values);
            }

            if (uri != null) {
                File file = new File(filename);
                if (file.isFile() && file.exists()) {
                    try {
                        OutputStream outputStream = contentResolver.openOutputStream(uri);
                        //2、建立数据通道
                        FileInputStream fileInputStream = new FileInputStream(file);
                        byte[] buf = new byte[1024 * 500];
                        int length = 0;
                        while ((length = fileInputStream.read(buf)) != -1) {
                            outputStream.write(buf, 0, length);
                        }
                        fileInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    file.delete();
                }
            }
        } else {
            try {
                ContentResolver contentResolver = singleton.getContentResolver();
                final ContentValues values = new ContentValues();
                if (bPhoto) {
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    values.put(MediaStore.Images.Media.DATA, filename);
                    Uri uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            values);
                } else {
                    values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
                    values.put(MediaStore.Video.Media.DATA, filename);
                    Uri uri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            values);
                }
//                Uri uri1 = Uri.parse("file://" + filename);
//                singleton.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri1));
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

//    @Override
//    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
//        EventBus.getDefault().register(activity);
//
//    }
//
//    @Override
//    public void onActivityStarted(Activity activity) {
//        activityAount++;
//        bGotsystemActivity = false;
//    }
//
//    @Override
//    public void onActivityResumed(Activity activity) {
//        JoyApp.F_MakeFullScreen(activity);
//
//    }
//
//    @Override
//    public void onActivityPaused(Activity activity) {
//
//    }
//
//    @Override
//    public void onActivityStopped(Activity activity) {
//        activityAount--;
//        if (activityAount == 0) {
//            if (!bGotsystemActivity) {
//                EventBus.getDefault().post("", "Go2Background");
//            }
//        }
//
//    }
//
//    @Override
//    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
//
//    }
//
//    @Override
//    public void onActivityDestroyed(Activity activity) {
//        EventBus.getDefault().unregister(activity);
//    }

    public static  void F_ExitJoyAPP()
    {
        EventBus.getDefault().post("","Go2Background");
    }

}
