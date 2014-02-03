package com.samsung.downloadservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.samsung.models.DownloadRequest;

/**
 * Created by mkluver on 1/30/14.
 */
public class DownloadRequestBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("MARC", "DownloadRequestBroadcastReceiver :: onRecieve");
        Intent startServiceIntent = new Intent(context, DownloadService.class);

        DownloadRequest downloadRequest = (DownloadRequest)intent.getSerializableExtra(DownloadRequest.INTENT_EXTRA_KEY);

        startServiceIntent.putExtra(DownloadRequest.INTENT_EXTRA_KEY, downloadRequest);

        Log.i("MARC" ,"DownloadRequestBroadcastReceiver::Path= "+downloadRequest.path+"   URL "+downloadRequest.url);


        context.startService(startServiceIntent);
    }
}
