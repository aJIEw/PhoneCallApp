package com.ajiew.phonecallapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.role.RoleManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.ajiew.phonecallapp.listenphonecall.CallListenerService;

import java.lang.reflect.Field;


public class MainActivity extends AppCompatActivity {

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switchPhoneCall;

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switchListenCall;

    private CompoundButton.OnCheckedChangeListener switchCallCheckChangeListener;

    private final ActivityResultLauncher<Intent> dialerRequestLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Toast.makeText(MainActivity.this, getString(R.string.app_name) + " 已成为默认电话应用",
                            Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        switchPhoneCall = findViewById(R.id.switch_default_phone_call);
        switchListenCall = findViewById(R.id.switch_call_listenr);

        switchPhoneCall.setOnClickListener(v -> {
            // 发起将本应用设为默认电话应用的请求，仅支持 Android M 及以上
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (switchPhoneCall.isChecked()) {
                    // Android 10 之后需要通过 RoleManager 修改默认电话应用
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        RoleManager roleManager = (RoleManager) getSystemService(Context.ROLE_SERVICE);
                        Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER);
                        dialerRequestLauncher.launch(intent);
                    } else {
                        Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
                        intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, getPackageName());
                        startActivity(intent);
                    }
                } else {
                    // 取消时跳转到默认设置页面
                    startActivity(new Intent("android.settings.MANAGE_DEFAULT_APPS_SETTINGS"));
                }
            } else {
                Toast.makeText(MainActivity.this, "Android 6.0 以上才支持修改默认电话应用！", Toast.LENGTH_LONG)
                     .show();
                switchPhoneCall.setChecked(false);
            }

        });

        // 检查是否开启了权限
        switchCallCheckChangeListener = (buttonView, isChecked) -> {
            if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && !Settings.canDrawOverlays(MainActivity.this)) {
                // 请求 悬浮框 权限
                askForDrawOverlay();

                // 未开启时清除选中状态，同时避免回调
                switchListenCall.setOnCheckedChangeListener(null);
                switchListenCall.setChecked(false);
                switchListenCall.setOnCheckedChangeListener(switchCallCheckChangeListener);
                return;
            }

            boolean granted = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
            if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !granted) {
                Toast.makeText(this, "缺少获取电话状态权限", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent callListener = new Intent(MainActivity.this, CallListenerService.class);
            if (isChecked) {
                startService(callListener);
                Toast.makeText(this, "电话监听服务已开启", Toast.LENGTH_SHORT).show();
            } else {
                stopService(callListener);
                Toast.makeText(this, "电话监听服务已关闭", Toast.LENGTH_SHORT).show();
            }
        };
        switchListenCall.setOnCheckedChangeListener(switchCallCheckChangeListener);
    }

    private void askForDrawOverlay() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("允许显示悬浮框")
                .setMessage("为了使电话监听服务正常工作，请允许这项权限")
                .setPositiveButton("去设置", (dialog, which) -> {
                    openDrawOverlaySettings();
                    dialog.dismiss();
                })
                .setNegativeButton("稍后再说", (dialog, which) -> dialog.dismiss())
                .create();

        //noinspection ConstantConditions
        alertDialog.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        alertDialog.show();
    }

    /**
     * 跳转悬浮窗管理设置界面
     */
    private void openDrawOverlaySettings() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M 以上引导用户去系统设置中打开允许悬浮窗
            // 使用反射是为了用尽可能少的代码保证在大部分机型上都可用
            try {
                Context context = this;
                Class clazz = Settings.class;
                Field field = clazz.getDeclaredField("ACTION_MANAGE_OVERLAY_PERMISSION");
                Intent intent = new Intent(field.get(null).toString());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "请在悬浮窗管理中打开权限", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        switchPhoneCall.setChecked(isDefaultPhoneCallApp());
        switchListenCall.setChecked(isServiceRunning(CallListenerService.class));
    }

    /**
     * Android M 及以上检查是否是系统默认电话应用
     */
    public boolean isDefaultPhoneCallApp() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            TelecomManager manger = (TelecomManager) getSystemService(TELECOM_SERVICE);
            if (manger != null && manger.getDefaultDialerPackage() != null) {
                return manger.getDefaultDialerPackage().equals(getPackageName());
            }
        }
        return false;
    }

    public boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null) return false;

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }

        return false;
    }
}
