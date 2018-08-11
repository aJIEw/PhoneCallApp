package com.ajiew.phonecallapp.listenphonecall;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ajiew.phonecallapp.MainActivity;
import com.ajiew.phonecallapp.R;

/**
 * author: aJIEw
 * description: 电话状态变化后运行的服务
 * 这里我在服务中启动了一个系统级弹窗，在通话的时候就显示，
 * 然后在其中放了一个按钮用于打开 PhoneCallApp
 */
public class CallListenerService extends Service {

    private static final String TAG = CallListenerService.class.getSimpleName();

    public static boolean isRunning;

    private View phoneCallView;
    private TextView tvCallNumber;
    private Button btnOpenApp;

    private WindowManager windowManager;
    private WindowManager.LayoutParams params;

    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;

    private String callNumber;
    private boolean hasShown;
    private boolean isCallingIn;
    private boolean isCallingOut;

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "支持修改默认电话应用，无需运行本此Service");
            return; // 如果为了看效果又不想创建 6.0 以下的模拟器可以注释掉这行
        }

        isRunning = true;

        initPhoneStateListener();

        initPhoneCallView();
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
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);

                callNumber = incomingNumber;

                switch (state) {
                    case TelephonyManager.CALL_STATE_IDLE: // 待机，即无电话时，挂断时触发
                        dismiss();
                        break;

                    case TelephonyManager.CALL_STATE_RINGING: // 响铃，来电时触发
                        isCallingIn = true;
                        updateUI();
                        break;

                    case TelephonyManager.CALL_STATE_OFFHOOK: // 摘机，接听或打电话时触发
                        updateUI();
                        show();
                        break;

                    default:
                        break;

                }
            }
        };

        // 设置来电监听器
        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }

    }

    private void initPhoneCallView() {
        windowManager = (WindowManager) getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        int width = windowManager.getDefaultDisplay().getWidth();
        int height = windowManager.getDefaultDisplay().getHeight();

        params = new WindowManager.LayoutParams();
        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
        params.width = width;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

        // 设置图片格式，效果为背景透明
        params.format = PixelFormat.TRANSLUCENT;
        // 设置 Window flag 为系统级弹框 | 覆盖表层
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;

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

        phoneCallView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.view_phone_call, interceptorLayout);
        tvCallNumber = phoneCallView.findViewById(R.id.tv_call_number);
        btnOpenApp = phoneCallView.findViewById(R.id.btn_open_app);
        btnOpenApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                CallListenerService.this.startActivity(intent);
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && Intent.ACTION_NEW_OUTGOING_CALL.equals(intent.getAction())) {
            callNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            isCallingOut = true;
        }

        return START_STICKY;
    }

    /**
     * 显示顶级弹框展示通话信息
     */
    private void show() {
        if (!hasShown) {
            windowManager.addView(phoneCallView, params);
            hasShown = true;
        }
    }

    /**
     * 取消显示
     */
    private void dismiss() {
        if (hasShown) {
            windowManager.removeView(phoneCallView);
            isCallingIn = false;
            isCallingOut = false;
            hasShown = false;
        }
    }

    private void updateUI() {
        tvCallNumber.setText(formatPhoneNumber(callNumber));

        int callTypeDrawable = isCallingIn ? R.drawable.ic_phone_call_in : R.drawable.ic_phone_call_out;
        tvCallNumber.setCompoundDrawablesWithIntrinsicBounds(null, null,
                getResources().getDrawable(callTypeDrawable), null);
    }

    public static String formatPhoneNumber(String phoneNum) {
        if (!TextUtils.isEmpty(phoneNum) && phoneNum.length() == 11) {
            return phoneNum.substring(0, 3) + "-"
                    + phoneNum.substring(3, 7) + "-"
                    + phoneNum.substring(7);
        }
        return phoneNum;
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        sendBroadcast(new Intent(KeepRunningReceiver.AUTO_START_RECEIVER));

        super.onDestroy();
    }
}
