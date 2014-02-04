package com.samsung.installersystemservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.samsung.installersystemservice.apihelper.ApplicationManager;
import com.samsung.installersystemservice.apihelper.OnInstalledPackaged;
import com.samsung.models.DownloadRequest;

/**
 * Created by mkluver on 2/3/14.
 */
public class DownloadDoneBroadcastReceiver extends BroadcastReceiver {
    private ApplicationManager am;
    static final String TAG = "MARC";


    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i("MARC", "DownloadDone - BroadcastReceiver :: onRecieve");

        DownloadRequest downloadRequest = (DownloadRequest)intent.getSerializableExtra(DownloadRequest.INTENT_EXTRA_KEY);

        Log.i("MARC" ,"DownloadDone :Path= "+downloadRequest.path+"   URL "+downloadRequest.url);

        Log.i("MARC" ,"Now we can do the real download ");

        try {
            am = new ApplicationManager(context);

            am.setOnInstalledPackaged(new OnInstalledPackaged() {

                public void packageInstalled(String packageName, int returnCode) {
                    if (returnCode == ApplicationManager.INSTALL_SUCCEEDED) {
                        Log.d(TAG, "HAHA Install succeeded "+packageName);
                    } else {
                        Log.d(TAG, "Install failed: " + returnCode+"  "+packageName);
                    }
                }
            });
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            Log.i(TAG," No such method "+e.getLocalizedMessage());
        }

        try {
            am.installPackage(downloadRequest.path);
            Log.d(TAG,"Done With Install no crash "+downloadRequest.path);
        } catch (Exception e) {
            Log.d(TAG,"Install ERROR "+e.getLocalizedMessage());
        }


    }
}
