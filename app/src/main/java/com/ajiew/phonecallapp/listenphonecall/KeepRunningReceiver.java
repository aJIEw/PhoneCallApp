package com.ajiew.phonecallapp.listenphonecall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * author: aJIEw
 * description: 保持 {@link CallListenerService} 在后台运行的广播接收器，开机启动
 */
public class KeepRunningReceiver extends BroadcastReceiver {

    public static final String AUTO_START_RECEIVER = "com.ajiew.phonecallapp.autostart_action";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (!CallListenerService.isRunning) {
            startCallShowService(context, intent);
        }
    }

    private void startCallShowService(Context context, Intent intent) {
        intent.setClass(context, CallListenerService.class);
        context.startService(intent);
    }

}
