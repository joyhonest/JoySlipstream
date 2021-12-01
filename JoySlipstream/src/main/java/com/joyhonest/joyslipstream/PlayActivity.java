package com.joyhonest.joyslipstream;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;

import com.joyhonest.joyslipstream.databinding.ActivityPlayBinding;
//import com.joyhonest.wifination.wifination;
import com.joyhonest.joycamera.sdk.*;

import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;

import java.util.Locale;

public class PlayActivity extends AppCompatActivity implements View.OnClickListener {


    private final String TAG = "PlayActivity";

    private ActivityPlayBinding binding;


    Handler OpenCamerapHandler;
    Handler DispWifiHandler;
    private HandlerThread thread1;

    private Joyh_PermissionAsker mAsker;
    int nAsk = -1;

    private AlertDialog alertDialog;

    //判断按钮是否按下
    private boolean bController = true;


    //private boolean   bHide = false;

    private boolean bVr = false;


    private boolean bFlying = false;
    private boolean bGyro = false;
    private boolean bHide_toolbar = false;


    WifiManager wifiManager;// = (WifiManager) getSystemService(WIFI_SERVICE)

    private Handler SentCmd_Handler;//= new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JoyApp.initJoySlipstream(this);
        JoyApp.F_MakeFullScreen(this);
        binding = ActivityPlayBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SentCmd_Handler = new Handler(getMainLooper());

        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        binding.myController.F_SetImage(R.mipmap.cir_back_fly_jh, R.mipmap.cir_fly_jh);
        binding.myController.F_SetDispText(false);
        binding.RecordTime.setVisibility(View.INVISIBLE);
        binding.DispImageView.setScaleType(ImageView.ScaleType.FIT_XY);


        binding.btnController.setOnClickListener(this);
        binding.btnAdjustment.setOnClickListener(this);
        binding.btnSpeedSwitch.setOnClickListener(this);
        binding.btnGallery.setOnClickListener(this);
        binding.btnMotionSensitive.setOnClickListener(this);
        binding.btnPath.setOnClickListener(this);
        binding.btnVr.setOnClickListener(this);
        binding.btnBackMain.setOnClickListener(this);
        binding.btnTakePhoto.setOnClickListener(this);
        binding.btnTakeVideo.setOnClickListener(this);
        binding.btnEngineSwitch.setOnClickListener(this);
        binding.btnHideToolbar.setOnClickListener(this);
        binding.btnAutoLaunchLand.setOnClickListener(this);


        thread1 = new HandlerThread("planerockerCmdThread");
        thread1.start(); //创建一个HandlerThread并启动它

        OpenCamerapHandler = new Handler(thread1.getLooper());//使用HandlerThread的looper对象创建Handler，如果使用默认的构造方法，很有可能阻塞UI线程
        DispWifiHandler = new Handler(getMainLooper());


        OpenCamerapHandler.removeCallbacksAndMessages(null);
        OpenCamerapHandler.post(OpenCamerapRunnable);
        DispWifiHandler.post(DispWifiRunnable);


        alertDialog = new AlertDialog.Builder(this)
                .setTitle("permission")
                .setMessage("The device needs to be allowed permission to function properly")
                .setNegativeButton("OK", (dialog, which) -> {
                    Joyh_PermissionPageUtils joyhPermissionPageUtils = new Joyh_PermissionPageUtils(PlayActivity.this);
                    JoyApp.bGotsystemActivity = true;
                    joyhPermissionPageUtils.jumpPermissionPage();
                }).create();


        if (JoyApp.isAndroidQ()) {
            F_GetPermissions();
        }
        mAsker = new Joyh_PermissionAsker(10, new Runnable() {
            @Override
            public void run() {
                F_GetPermissions();
            }
        }, new Runnable() {
            @Override
            public void run() {
                alertDialog.show();
            }
        });

        SentCmd_Handler.post(sentCmdRunnable);
        EventBus.getDefault().register(this);

    }


    void F_GetPermissions() {
        JoyApp.F_CreateLocalDir("Slipstream-WiFi");
        if (nAsk == 0) {
            if (JoyApp.bConnected) {
                JoyApp.PlayPhotoMusic();
                String sName = JoyApp.getFileNameFromDate(false);
                //wifination.naSnapPhoto(sName, wifination.TYPE_ONLY_PHONE);
                wifiCamera.naSnapPhoto(sName, wifiCamera.TYPE_ONLY_PHONE, wifiCamera.TYPE_DEST_SNADBOX);
            }

        } else if (nAsk == 1) {
            if (JoyApp.bConnected) {
                JoyApp.PlayBtnVoice();
//                if (wifination.isPhoneRecording()) {
//                    wifination.naStopRecord_All();
//                } else {
//                    String sName = JoyApp.getFileNameFromDate(true);
//                    wifination.naStartRecord(sName, wifination.TYPE_ONLY_PHONE);
//                }
                if (wifiCamera.isPhoneRecording()) {
                    wifiCamera.naStopRecordAll();
                } else {
                    String sName = JoyApp.getFileNameFromDate(true);
                    wifiCamera.naStartRecord(sName, wifiCamera.TYPE_ONLY_PHONE, wifiCamera.TYPE_DEST_SNADBOX, false);
                }

            }
        } else if (nAsk == 2) {
            JoyApp.PlayBtnVoice();
            //wifination.naStopRecord_All();
            wifiCamera.naStopRecordAll();
            startActivity(new Intent(PlayActivity.this, GalleryActivity.class));
            overridePendingTransition(0, 0);
        }
        nAsk = -1;

    }


    @Override
    protected void onStart() {
        super.onStart();
        JoyApp.bGotsystemActivity = false;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_controller) {
            JoyApp.PlayBtnVoice();
            if (binding.myController.getVisibility() == View.VISIBLE) {
                bController = false;
                binding.myController.setVisibility(View.INVISIBLE);
                binding.btnController.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.cyan)));
            } else {
                bController = true;
                binding.myController.setVisibility(View.VISIBLE);
                binding.btnController.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
            }
        } else if (id == R.id.btn_adjustment) {
            JoyApp.PlayBtnVoice();
            JoyApp.bCorrection_adj = true;
            if (JoyApp.bCorrection_adj) {
                binding.btnAdjustment.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.cyan)));
            }
            new Handler(this.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    JoyApp.bCorrection_adj = false;
                    binding.btnAdjustment.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(PlayActivity.this, R.color.white)));
                }
            }, 500);


        } else if (R.id.btn_speed_switch == id) {
            JoyApp.PlayBtnVoice();
            JoyApp.bHSpeed = !JoyApp.bHSpeed;
            if (JoyApp.bHSpeed) {
                binding.btnSpeedSwitch.setBackgroundResource(R.mipmap.speed_h);
            } else {
                binding.btnSpeedSwitch.setBackgroundResource(R.mipmap.speed_jh);
            }
        } else if (id == R.id.btn_take_photo) {
            if (JoyApp.bConnected) {
                F_CheckPermissions(0);
            }
        } else if (id == R.id.btn_take_video) {
            if (JoyApp.bConnected) {
                F_CheckPermissions(1);
            }
        } else if (id == R.id.btn_gallery) {
            F_CheckPermissions(2);

        } else if (id == R.id.btn_motion_sensitive) {
            JoyApp.PlayBtnVoice();
            bGyro = !bGyro;
            if (bGyro) {
                binding.btnMotionSensitive.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.cyan)));
                binding.myController.F_SetMode(1);
                int org = getWindowManager().getDefaultDisplay().getRotation();
                if (Surface.ROTATION_270 == org) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            } else {
                binding.btnMotionSensitive.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
                binding.myController.F_SetMode(0);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            }


        } else if (R.id.btn_path == id) {
            JoyApp.PlayBtnVoice();
            JoyApp.bPath = true;
            bVr = false;
            wifiCamera.naSetVR(bVr);
            if (!JoyApp.bConnected) {
                if (bVr) {
                    binding.DispImageView.setBackgroundResource(R.mipmap.vr_back_jh);
                } else {
                    binding.DispImageView.setBackgroundResource(R.mipmap.vr_no_back_jh);
                }
            }
            startActivity(new Intent(PlayActivity.this, RockerSettingActivitiy.class));
            overridePendingTransition(0, 0);
        } else if (R.id.btn_vr == id) {
            JoyApp.PlayBtnVoice();
            bVr = !bVr;
            //wifination.naSet3D(bVr);
            wifiCamera.naSetVR(bVr);
            if (!JoyApp.bConnected) {
                if (bVr) {
                    binding.DispImageView.setBackgroundResource(R.mipmap.vr_back_jh);
                } else {
                    binding.DispImageView.setBackgroundResource(R.mipmap.vr_no_back_jh);
                }
            }
        } else if (id == R.id.btn_back_main) {
            JoyApp.PlayBtnVoice();
            onBackPressed();
        } else if (R.id.btn_engine_switch == id) {
            JoyApp.PlayBtnVoice();
            JoyApp.bStop = true;
            JoyApp.bDn = false;
            JoyApp.bDn = false;
            binding.btnEngineSwitch.setEnabled(false);
            binding.btnAutoLaunchLand.setEnabled(false);

            new Handler(getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    JoyApp.bUp = false;
                    JoyApp.bDn = false;
                    JoyApp.bStop = false;
                    bFlying = false;
                    binding.btnEngineSwitch.setEnabled(true);
                    binding.btnAutoLaunchLand.setEnabled(true);
                }
            }, 800);

        } else if (id == R.id.btn_hide_toolbar) {
            JoyApp.PlayBtnVoice();
            bHide_toolbar = !bHide_toolbar;
            int ds = binding.DispImageView.getHeight() - binding.btnHideToolbar.getBottom();
            float f = ds;
            if (bHide_toolbar) {

                binding.btnHideToolbar.setBackgroundResource(R.mipmap.control_toolbar_up);
                ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(binding.btnHideToolbar, "translationY", 0f, f);
                objectAnimator.setDuration(10);
                objectAnimator.start();
                binding.layToolbarMain.setVisibility(View.INVISIBLE);
            } else {
                binding.btnHideToolbar.setBackgroundResource(R.mipmap.control_toolbar);
                binding.layToolbarMain.setVisibility(View.VISIBLE);
                ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(binding.btnHideToolbar, "translationY", f, 0f);
                objectAnimator.setDuration(10);
                objectAnimator.start();
            }
        } else if (id == R.id.btn_auto_launch_land) {
            JoyApp.PlayBtnVoice();

            bFlying = !bFlying;
            JoyApp.bStop = false;
            if (bFlying) {
                JoyApp.bUp = true;
                JoyApp.bDn = false;
            } else {
                JoyApp.bUp = false;
                JoyApp.bDn = true;
            }

            new Handler(getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    JoyApp.bUp = false;
                    JoyApp.bDn = false;
                    JoyApp.bStop = false;
                    //bFlying = false;
                }
            }, 800);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mAsker.onRequestPermissionsResult(grantResults);
    }

    void F_CheckPermissions(int nA) {
        nAsk = nA;
        if (JoyApp.isAndroidQ()) {
            F_GetPermissions();
        } else {
            mAsker.askPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    Runnable OpenCamerapRunnable = new Runnable() {
        @Override
        public void run() {
            //wifination.naSetRevBmp(true);
            //wifination.naInit("");
            wifiCamera.naSetReceiveBmp(true);
            wifiCamera.naInit("");
            JoyApp.bConnected = false;
        }
    };

    @Subscriber(tag = "onGetFrame")
    private void onGetFrame(Bitmap bitmap) {
        binding.DispImageView.setImageBitmap(bitmap);
        JoyApp.bConnected = true;

    }


    //设置视频的计时器开启和关闭
    @Subscriber(tag = "onCameraStatusChange")
    private void onStatusChange(Integer nStatus) {
        //#define  bit0_OnLine            1
        //#define  bit1_LocalRecording    2
        //#define  SD_Ready               4
        //#define  SD_Recroding           8
        //#define  SD_Photo               0x10
        Log.d("   Status", "" + nStatus);
        if ((nStatus & 0x02) != 0) {
            if (!JoyApp.bRecording) {
                JoyApp.bRecording = true;
                F_DispRecordTime(true);
            }


        } else {
            if (JoyApp.bRecording) {
                JoyApp.bRecording = false;
                F_DispRecordTime(false);
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Subscriber(tag = "SavePhotoOK")
    private void SavePhotoOK(String Sn) {
        if (Sn.length() < 5) {
            return;
        }
        String sType = Sn.substring(0, 2);
        String sName = Sn.substring(2, Sn.length());
        int nPhoto = Integer.parseInt(sType);
        if (nPhoto == 0) {
            JoyApp.F_Save2ToGallery(sName, true);

        } else {
            JoyApp.F_Save2ToGallery(sName, false);
        }
    }


    private Runnable sentCmdRunnable = new Runnable() {
        @Override
        public void run() {
            F_SentCmd();
            SentCmd_Handler.postDelayed(this, 20);
        }
    };


    byte[] cmd = new byte[20];

    private void F_SentCmd() {
        if (JoyApp.bPath) {
            return;
        }
        if (bController) {
            int i = 0;
            int X1, Y1, X2, Y2, X_ADJ2, Y_ADJ2, X_ADJ1;
            int X2_bak;
            int Y2_bak;

            X1 = binding.myController.F_GetRotate();
            Y1 = binding.myController.F_GetThrottle();
            X2 = binding.myController.F_GetLeftRight();
            Y2 = binding.myController.F_GetForwardBack();
            X_ADJ1 = binding.myController.F_GetRotateAdj();
            X_ADJ2 = binding.myController.F_GetLeftRightAdj();
            Y_ADJ2 = binding.myController.F_GetForwardBackAdj();

            //  Log.e(TAG,"X1 = "+X1);

            if (X2 > 0x70 && X2 < 0x90) {
                X2 = 0x80;
            }

            if (Y2 > 0x70 && Y2 < 0x90) {
                Y2 = 0x80;
            }

            if (X1 > (0x80 - 0x25) && X1 < (0x80 + 0x25)) {
                X1 = 0x80;
            }
            i = 0;


            if (Y2 > 0x80) {
                Y2 -= 0x80;
            } else if (Y2 < 0x80) {
                Y2 = 0x80 - Y2;
                Y2 += 0x80;
                if (Y2 > 0xFF) {
                    Y2 = 0xFF;
                }
            }

            if (X1 > 0x80) {
                ;
            } else if (X1 < 0x80) {
                X1 = 0x80 - X1;
                if (X1 > 0x7F) {
                    X1 = 0x7F;
                }
            }

            if (X2 > 0x80) {
            } else if (X2 < 0x80) {
                X2 = 0x80 - X2;
                if (X2 > 0x7F) {
                    X2 = 0x7F;
                }
            }


            cmd[0] = (byte) Y1;   //油门
            cmd[1] = (byte) Y2;
            cmd[2] = (byte) X1;
            cmd[3] = (byte) X2;

            cmd[4] = 0x20;          //油门微调  这里没有。

            int da = Y_ADJ2 - 0x80;
            if (da < 0)               // 后调
            {
                da = 0 - da;
                da += 0x20;
                if (da > 0x3F) {
                    da = 0x3F;
                }
            } else if (da > 0) {
                if (da > 0x1F)
                    da = 0x1F;
            } else {
                da = 0x20;
            }


            cmd[5] = (byte) da;       //前后微调
            if (JoyApp.bHSpeed)
                cmd[5] |= 0x80;          //高速模式

            da = X_ADJ1 - 0x80;          //旋转微调
            if (da < 0) {
                da = 0 - da;
                if (da > 0x1F) {
                    da = 0x1F;
                }
            } else if (da > 0) {
                da += 0x20;
                if (da > 0x3F)
                    da = 0x3F;
            } else {
                da = 0x20;
            }


            cmd[6] = (byte) da;


            da = X_ADJ2 - 0x80;
            if (da < 0) {

                da = 0 - da;
                if (da > 0x1F) {
                    da = 0x1F;
                }
            } else if (da > 0) {
                da += 0x20;
                if (da > 0x3F)
                    da = 0x3F;
            } else {
                da = 0x20;
            }

            cmd[7] = (byte) da;        //平移

            if (JoyApp.bUp) {
                cmd[7] |= 0x40;
            }
            if (JoyApp.bCompass) {
                cmd[7] |= 0x80;
            }

            cmd[8] = 0;

            if (JoyApp.bStop) {
                cmd[8] |= 0x10;
            }
            if (JoyApp.bCorrection_adj) {
                cmd[8] |= 0x20;
            }

            if (JoyApp.bDn) {
                cmd[8] |= 0x08;
            }
            cmd[9] = (byte) (((cmd[0] ^ cmd[1] ^ cmd[2] ^ cmd[3] ^ cmd[4] ^ cmd[5] ^ cmd[6] ^ cmd[7] ^ cmd[8]) & 0xFF) + 0x55);
            wifiCamera.naSentCmd(cmd, 10);
        }

    }


    private void F_DispRecordTime(boolean b) {
        if (b) {
            binding.RecordTime.setVisibility(View.VISIBLE);
            Hand_DispRec.removeCallbacksAndMessages(null);
            //开启无限循环
            Hand_DispRec.post(Runnable_DispRec);
        } else {
            binding.RecordTime.setVisibility(View.INVISIBLE);
            Hand_DispRec.removeCallbacksAndMessages(null);
        }
    }

    Handler Hand_DispRec = new Handler();
    //发布后，每250毫秒获取一次录像时间
    Runnable Runnable_DispRec = new Runnable() {
        @Override
        public void run() {
            //int nSec = wifination.naGetRecordTime() / 1000;
            int nSec = (int) wifiCamera.naGetRecordTimems() / 1000;
            int nMin = nSec / 60;
            nSec = nSec % 60;
            int nHour = nMin / 60;
            nMin = nMin % 60;
            String str = "";
            if (nHour == 0)
                str = String.format(Locale.ENGLISH, "%02d:%02d", nMin, nSec);
            else
                str = String.format(Locale.ENGLISH, "%02d:%02d:%02d", nHour, nMin, nSec);
            binding.RecordTime.setText(str);
            Hand_DispRec.postDelayed(this, 250);
        }
    };


    @Subscriber(tag = "Go2Background")
    private void Go2Background(String str) {
        if (!JoyApp.bGotsystemActivity) {
            finish();

            overridePendingTransition(0, 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
//        wifination.naStopRecord_All();
//        wifination.naStop();
        wifiCamera.naStopRecordAll();
        wifiCamera.naStop();
        thread1.quit();
        OpenCamerapHandler.removeCallbacksAndMessages(null);
        DispWifiHandler.removeCallbacksAndMessages(null);
        SentCmd_Handler.removeCallbacksAndMessages(null);

    }

    @Override
    protected void onResume() {
        super.onResume();
        JoyApp.F_MakeFullScreen(this);
    }


    int bmp[] = {R.mipmap.wifi_jh, R.mipmap.wifistrength_1_jh, R.mipmap.wifistrength_2_jh, R.mipmap.wifistrength_3_jh, R.mipmap.wifistrength_4_jh};
    Runnable DispWifiRunnable = new Runnable() {
        @Override
        public void run() {
            if (JoyApp.bConnected) {
                WifiInfo info = wifiManager.getConnectionInfo();
                if (info != null) {
                    int strength = WifiManager.calculateSignalLevel(info.getRssi(), 5);
                    if (strength > 4)
                        strength = 4;
                    if (strength < 1)
                        strength = 0;
                    binding.imgWifi.setImageResource(bmp[strength]);
                }
            } else {
                binding.imgWifi.setImageResource(R.mipmap.wifi_jh);
            }
            DispWifiHandler.postDelayed(this, 1500);
        }
    };


}