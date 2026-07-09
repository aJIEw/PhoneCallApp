package com.ajiew.phonecallapp.listenphonecall;

import android.content.Context;
import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import androidx.annotation.RequiresApi;

/**
 * 电话状态监听器，API 31+ 使用 {@link android.telephony.TelephonyCallback.CallStateListener}。
 *
 * @author aJIEw
 * Created on: 2025/5/6 16:45
 */
public abstract class PhoneStateListenerCompat {
    private PhoneStateListener phoneStateListener;
    private CallStateTelephonyCallback callStateListener;

    @SuppressWarnings("deprecation")
    public void startListening(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(
                Context.TELEPHONY_SERVICE);
        if (tm == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            callStateListener = new CallStateTelephonyCallback() {
                @Override
                public void onCallStateChanged(int state) {
                    PhoneStateListenerCompat.this.onCallStateChanged(state, null);
                }
            };
            tm.registerTelephonyCallback(context.getMainExecutor(), callStateListener);
        } else {
            phoneStateListener = new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String phoneNumber) {
                    PhoneStateListenerCompat.this.onCallStateChanged(state, phoneNumber);
                }
            };
            startLegacyListening(tm, phoneStateListener);
        }
    }

    public void stopListening(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(
                Context.TELEPHONY_SERVICE);
        if (tm == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (callStateListener != null) {
                tm.unregisterTelephonyCallback(callStateListener);
                callStateListener = null;
            }
        } else {
            if (phoneStateListener != null) {
                stopLegacyListening(tm, phoneStateListener);
                phoneStateListener = null;
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void startLegacyListening(TelephonyManager tm, PhoneStateListener listener) {
        tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    @SuppressWarnings("deprecation")
    private void stopLegacyListening(TelephonyManager tm, PhoneStateListener listener) {
        tm.listen(listener, PhoneStateListener.LISTEN_NONE);
    }

    public abstract void onCallStateChanged(int state, String phoneNumber);
}

@RequiresApi(api = Build.VERSION_CODES.S)
abstract class CallStateTelephonyCallback extends android.telephony.TelephonyCallback
        implements android.telephony.TelephonyCallback.CallStateListener {
}
