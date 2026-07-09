package com.ajiew.phonecallapp.listenphonecall;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.annotation.Nullable;

import com.ajiew.phonecallapp.MainActivity;
import com.ajiew.phonecallapp.R;


public class CallListenerService extends Service {

    private View phoneCallView;
    private TextView tvCallNumber;
    private Button btnOpenApp;

    private WindowManager windowManager;
    private WindowManager.LayoutParams params;

    private PhoneStateListenerCompat phoneStateListenerCompat;

    private String callNumber;
    private boolean hasShown;
    private boolean isCallingIn;

    @Override
    public void onCreate() {
        super.onCreate();

        if (initPhoneCallView()) {
            initPhoneStateListener();
        } else {
            stopSelf();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 初始化来电状态监听器
     */
    private void initPhoneStateListener() {
        phoneStateListenerCompat = new PhoneStateListenerCompat() {
            @Override
            public void onCallStateChanged(int state, String phoneNumber) {
                if (phoneNumber != null && !phoneNumber.isEmpty()) {
                    callNumber = phoneNumber;
                }

                switch (state) {
                    case TelephonyManager.CALL_STATE_IDLE: // 待机，即无电话时，挂断时触发
                        dismiss();
                        callNumber = null;
                        break;

                    case TelephonyManager.CALL_STATE_RINGING: // 响铃，来电时触发
                        isCallingIn = true;
                        updateUI();
                        show();
                        break;

                    case TelephonyManager.CALL_STATE_OFFHOOK: // 摘机，接听或拨出电话时触发
                        updateUI();
                        show();
                        break;

                    default:
                        break;

                }
            }
        };
        phoneStateListenerCompat.startListening(this);
    }

    private boolean initPhoneCallView() {
        windowManager = (WindowManager) getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) {
            return false;
        }

        params = new WindowManager.LayoutParams();
        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

        // 设置图片格式，效果为背景透明
        params.format = PixelFormat.TRANSLUCENT;
        // 设置 Window flag 为系统级弹框 | 覆盖表层
        params.type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                WindowManager.LayoutParams.TYPE_PHONE;

        // 不可聚集（不响应返回键）| 全屏
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        // API 19 以上则还可以开启透明状态栏与导航栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            params.flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_FULLSCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        }

        FrameLayout interceptorLayout = new FrameLayout(this) {

            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {

                        return true;
                    }
                }

                return super.dispatchKeyEvent(event);
            }
        };

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (layoutInflater == null) {
            return false;
        }

        phoneCallView = layoutInflater.inflate(R.layout.view_phone_call, interceptorLayout);
        tvCallNumber = phoneCallView.findViewById(R.id.tv_call_number);
        btnOpenApp = phoneCallView.findViewById(R.id.btn_open_app);
        if (tvCallNumber == null || btnOpenApp == null) {
            return false;
        }
        btnOpenApp.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            CallListenerService.this.startActivity(intent);
        });
        return true;
    }

    /**
     * 显示顶级弹框展示通话信息
     */
    private void show() {
        if (!hasShown && windowManager != null && phoneCallView != null && params != null) {
            windowManager.addView(phoneCallView, params);
            hasShown = true;
        }
    }

    /**
     * 取消显示
     */
    private void dismiss() {
        if (hasShown && windowManager != null && phoneCallView != null) {
            windowManager.removeView(phoneCallView);
            isCallingIn = false;
            hasShown = false;
        }
    }

    private void updateUI() {
        if (callNumber != null && !callNumber.isEmpty()) {
            tvCallNumber.setText(formatPhoneNumber(callNumber));
        }

        int callTypeDrawable = isCallingIn ? R.drawable.ic_phone_call_in : R.drawable.ic_phone_call_out;
        tvCallNumber.setCompoundDrawablesWithIntrinsicBounds(null, null,
                ContextCompat.getDrawable(this, callTypeDrawable), null);
    }

    public static String formatPhoneNumber(String phoneNum) {
        if (phoneNum == null || phoneNum.isEmpty()) {
            return "未知号码";
        }
        if (phoneNum.length() == 11) {
            return phoneNum.substring(0, 3) + "-"
                    + phoneNum.substring(3, 7) + "-"
                    + phoneNum.substring(7);
        }
        return phoneNum;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (phoneStateListenerCompat != null) {
            phoneStateListenerCompat.stopListening(this);
        }
    }
}
