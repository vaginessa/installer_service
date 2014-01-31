package com.samsung.installersystemservice;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * Created by mkluver on 1/29/14.
 */
public class InstallerService extends IntentService  {
    private static final String TAG = "MARC";

    /**
     * Creates an DownloadService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public InstallerService(String name) {
        super(name);

        Log.i(TAG,"Constructing installer service");

        IntentFilter filter = new IntentFilter();
        filter.addAction("DOWNLOAD_DONE_ACTION");

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, "On Receive Broadcast in InstallerService ");
            }
        },null);

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG,"Handling Intent for installer service");
        Intent downloadIntent = new Intent();
        downloadIntent.setClassName("com.samsung.installersystemservice",  "com.samsung.installersystemservice.SystemStartupBroadcastReceiver");
        startService(downloadIntent);
    }
}
