package com.samsung.downloadservice;

import android.app.IntentService;
import android.content.Intent;
import android.os.*;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by mkluver on 1/29/14.
 */
public class DownloadService extends IntentService {
    private static final String TAG = "MARC";

    /**
     * Creates an DownloadService.  Invoked by your subclass's constructor.
     *
     */
    public DownloadService() {
        super("Download Service");
        Log.i(TAG,"DownloadService constructor called");
    }

    interface OnDownloadDoneListener {
        public void onDownloadDone(DownloadRequest downloadRequest);
    }
    

    private void download(final DownloadRequest downloadRequest, final OnDownloadDoneListener downloadDoneListener){
        new Thread(new Runnable(){public void run() {

            try {
                Log.i(TAG,"Download Thread running");

                URL url = new URL(downloadRequest.url);
                File file = new File(downloadRequest.path);
                Log.i(TAG, "Downloading " + downloadRequest.url + " --> " + file);

                HttpURLConnection httpConnection = (HttpURLConnection)url.openConnection();
                InputStream is = httpConnection.getInputStream();
                if (file.exists()) file.delete();

                FileOutputStream fos = new FileOutputStream(file);
                byte[] buffer = new byte[2048];
                int totalBytes=0;
                int length = httpConnection.getContentLength();
                while (is.available()>0 || totalBytes < length){
                    int bytesRead = is.available();
                    if (bytesRead>2048){
                        bytesRead=2048;
                    }
                    totalBytes += bytesRead;
                    is.read(buffer, 0, bytesRead);
                    fos.write(buffer);

                    int percent = (int)(((float)totalBytes/(float)length)*100);
                    Log.i(TAG, "Downloading " + percent + " %");
                }
                fos.flush();
                fos.close();

                downloadDoneListener.onDownloadDone(downloadRequest);
                
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                Log.d(TAG, "Error: " + e);
            }

        }}).start();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Log.i(TAG,"DownloadService :: onHandleIntent ");
            //DownloadRequest downloadRequest = (DownloadRequest)intent.getSerializableExtra("DOWNLOAD_REQUEST");


        }
        catch (ClassCastException e){
            e.printStackTrace();
        }
    }


    @Override
    public void onCreate() {


        Log.i(TAG,"onCreate download service");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"Download Service::onStartCommand ");
        return  0;
    }


}
