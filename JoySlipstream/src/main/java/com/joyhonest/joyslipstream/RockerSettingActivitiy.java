package com.joyhonest.joyslipstream;

import android.graphics.Bitmap;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;

import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.joyhonest.joycamera.sdk.wifiCamera;
import com.joyhonest.joyslipstream.databinding.ActivityRockerSettingActivitiyBinding;


import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;

public class RockerSettingActivitiy extends AppCompatActivity {
    private int leftCount=1;
    private int rightCoun=1;

    private Handler DispWifiHandler;

    private final String TAG = "RockerSettingActivitiy";
    ActivityRockerSettingActivitiyBinding binding;
    private Handler SentCmd_Handler;//= new Handler();
    private boolean bStop;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityRockerSettingActivitiyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        bStop = false;
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        SentCmd_Handler = new Handler(getMainLooper());

        DispWifiHandler = new Handler(getMainLooper());

        binding.myController.F_DispPahtView(true);
        binding.myController.F_SetImage(R.mipmap.cir_back_fly_jh, R.mipmap.cir_fly_jh);
        binding.myController.F_SetDispText(false);
        binding.btnBackSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JoyApp.PlayBtnVoice();
                onBackPressed();
            }
        });
        binding.engineSwitchSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bStop = true;
            }
        });

        SentCmd_Handler.post(sentCmdRunnable);
        DispWifiHandler.post(DispWifiRunnable);

        EventBus.getDefault().register(this);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        DispWifiHandler.removeCallbacksAndMessages(null);
        SentCmd_Handler.removeCallbacksAndMessages(null);
        JoyApp.bPath = false;
    }


    @Override
    protected void onResume() {
        super.onResume();
        JoyApp.F_MakeFullScreen(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        JoyApp.bGotsystemActivity = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        DispWifiHandler.removeCallbacksAndMessages(null);
        SentCmd_Handler.removeCallbacksAndMessages(null);
    }


    @Subscriber(tag = "Send2_path")
    public void Send2_path(Integer n)
    {
        binding.myController.pathView.Send2_path(n);
    }


    @Subscriber(tag = "onGetFrame")
    private void onGetFrame(Bitmap bitmap) {
        binding.DispImageView.setImageBitmap(bitmap);
        JoyApp.bConnected = true;
    }

    @Subscriber(tag = "Go2Background")
    private void Go2Background(String str) {
        if(!JoyApp.bGotsystemActivity) {
            JoyApp.bPath = false;
            finish();
            overridePendingTransition(0, 0);
        }
    }



    private Runnable sentCmdRunnable =  new Runnable() {
        @Override
        public void run() {
            F_SentCmd();
            SentCmd_Handler.postDelayed(this,20);
        }
    };

    WifiManager wifiManager;// = (WifiManager) getSystemService(WIFI_SERVICE)

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
                    binding.wifiSetting.setImageResource(bmp[strength]);
                }
            } else {
                binding.wifiSetting.setImageResource(R.mipmap.wifi_jh);
            }
            DispWifiHandler.postDelayed(this, 1500);
        }
    };


    byte[] cmd = new byte[20];
    private void F_SentCmd()
    {
        //if(bController)
        {
            if(!JoyApp.bPath)
            {
                return;
            }

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
            //wifination.naSentCmd(cmd, 10);
            wifiCamera.naSentCmd(cmd, 10);
        }

    }

}