package com.ajiew.phonecallapp.phonecallui;

import android.net.Uri;
import android.os.Build;
import android.telecom.Call;
import android.telecom.InCallService;

import androidx.annotation.RequiresApi;

import com.ajiew.phonecallapp.ActivityStack;


/**
 * 监听电话通信状态的服务，实现该类的同时必须提供电话管理的 UI
 *
 * @author aJIEw
 * @see PhoneCallActivity
 * @see android.telecom.InCallService
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public class PhoneCallService extends InCallService {

    private final Call.Callback callback = new Call.Callback() {
        @Override
        public void onStateChanged(Call call, int state) {
            super.onStateChanged(call, state);

            switch (state) {
                case Call.STATE_ACTIVE: {

                    break;
                }

                case Call.STATE_DISCONNECTED: {
                    ActivityStack.getInstance().finishActivity(PhoneCallActivity.class);
                    break;
                }

            }
        }
    };

    @Override
    public void onCallAdded(Call call) {
        super.onCallAdded(call);
        if (call == null) {
            return;
        }

        call.registerCallback(callback);
        PhoneCallManager.call = call;

        CallType callType = null;

        if (call.getState() == Call.STATE_RINGING) {
            callType = CallType.CALL_IN;
        } else if (call.getState() == Call.STATE_CONNECTING) {
            callType = CallType.CALL_OUT;
        }

        if (callType != null) {
            String phoneNumber = getPhoneNumber(call);
            PhoneCallActivity.actionStart(this, phoneNumber, callType);
        }
    }

    @Override
    public void onCallRemoved(Call call) {
        super.onCallRemoved(call);
        if (call == null) {
            PhoneCallManager.call = null;
            return;
        }

        call.unregisterCallback(callback);
        PhoneCallManager.call = null;
    }

    private String getPhoneNumber(Call call) {
        Call.Details details = call.getDetails();
        if (details == null) {
            return null;
        }

        Uri handle = details.getHandle();
        if (handle == null) {
            return null;
        }

        return handle.getSchemeSpecificPart();
    }

    public enum CallType {
        CALL_IN,
        CALL_OUT,
    }
}
