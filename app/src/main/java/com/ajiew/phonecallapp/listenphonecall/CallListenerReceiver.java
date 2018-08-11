package com.ajiew.phonecallapp.listenphonecall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * author: aJIEw
 * description: 拨出电话与电话状态变化的广播接收器
 */
public class CallListenerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null && action.equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            context.startService(new Intent(context, CallListenerService.class));
        }
    }
}
