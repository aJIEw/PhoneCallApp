package com.ajiew.phonecallapp.listenphonecall;

import android.content.Context;
import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.RequiresApi;

/**
 * 新的电话状态监听器，适配 {@link android.telephony.TelephonyCallback} 以及老版本的 {@link PhoneStateListener}
 *
 * @author aJIEw
 * Created on: 2025/5/6 16:45
 */
public abstract class PhoneStateListenerCompat {
    private PhoneStateListener phoneStateListener;
    private TelephonyCallback telephonyCallback;

    public void startListening(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(
                Context.TELEPHONY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyCallback = new TelephonyCallback() {
                @Override
                public void onCallStateChanged(int state) {
                    Log.d(PhoneStateListenerCompat.class.getSimpleName(), "onCallStateChanged: ");
                    PhoneStateListenerCompat.this.onCallStateChanged(state);
                }
            };
            tm.registerTelephonyCallback(context.getMainExecutor(), telephonyCallback);
        } else {
            phoneStateListener = new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String phoneNumber) {
                    PhoneStateListenerCompat.this.onCallStateChanged(state);
                }
            };
            tm.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    public void stopListening(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(
                Context.TELEPHONY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (telephonyCallback != null) {
                tm.unregisterTelephonyCallback(telephonyCallback);
            }
        } else {
            if (phoneStateListener != null) {
                tm.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
            }
        }
    }

    public abstract void onCallStateChanged(int state);
}

@RequiresApi(api = Build.VERSION_CODES.S)
class TelephonyCallback extends android.telephony.TelephonyCallback
        implements android.telephony.TelephonyCallback.CallStateListener {

    @Override
    public void onCallStateChanged(int state) {
        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                // 空闲状态
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                // 来电响铃
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                // 通话中
                break;
        }
    }
}
