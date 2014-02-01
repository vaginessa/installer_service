package com.samsung.downloadservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by mkluver on 1/30/14.
 */
public class DownloadRequestBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("MARC", "DownloadRequestBroadcastReceiver :: onRecieve");
        Intent startServiceIntent = new Intent(context, DownloadService.class);
        context.startService(startServiceIntent);
    }
}
