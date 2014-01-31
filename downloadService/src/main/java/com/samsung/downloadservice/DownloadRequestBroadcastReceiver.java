package com.samsung.downloadservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by mkluver on 1/30/14.
 */
public class DownloadRequestBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent startServiceIntent = new Intent(context, DownloadService.class);
        context.startService(startServiceIntent);
    }
}
