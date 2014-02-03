package com.samsung.installersystemservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.samsung.models.DownloadRequest;

/**
 * Created by mkluver on 2/3/14.
 */
public class DownloadDoneBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("MARC", "DownloadDone - BroadcastReceiver :: onRecieve");

        DownloadRequest downloadRequest = (DownloadRequest)intent.getSerializableExtra(DownloadRequest.INTENT_EXTRA_KEY);

        Log.i("MARC" ,"DownloadDone :Path= "+downloadRequest.path+"   URL "+downloadRequest.url);

        Log.i("MARC" ,"Now we can do the real download ");

    }
}
