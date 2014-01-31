package com.samsung.installersystemservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by mkluver on 1/30/14.
 */
public class SystemStartupBroadcastReceiver extends BroadcastReceiver {
    private static String TAG = "MARC";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Recieved brodcast from startup process");
        Intent startServiceIntent = new Intent(context, InstallerService.class);
        context.startService(startServiceIntent);
    }
}
