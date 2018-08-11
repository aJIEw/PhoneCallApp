package com.ajiew.phonecallapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.telecom.TelecomManager;

import com.ajiew.phonecallapp.listenphonecall.CallListenerService;

/**
 * author: aJIEw
 * description:
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Android M 以上的系统发起将本应用设为默认电话应用的请求
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
            intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
                    getPackageName());
            startActivity(intent);

            //startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
        }

        startService(new Intent(this, CallListenerService.class));
    }

}
