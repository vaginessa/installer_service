package com.samsung.downloadservice;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.samsung.models.DownloadRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
                File root = android.os.Environment.getExternalStorageDirectory();
                String appPath = getApplicationContext().getFilesDir().getAbsolutePath();
                URL url = new URL(downloadRequest.url);
                File file = new File(root,"the_file.apk");//downloadRequest.path);
                Log.i(TAG, "Downloading " + downloadRequest.url + " --> " + file);

                HttpURLConnection httpConnection = (HttpURLConnection)url.openConnection();
                InputStream is = httpConnection.getInputStream();
                if (file.exists()) file.delete();

                OutputStream fos = new FileOutputStream(file);
                byte[] buffer = new byte[2048];
                int totalBytes=0;
                int length = httpConnection.getContentLength();
                int oldPercent=-100;
                while (is.available()>0 || totalBytes < length){
                    int bytesRead = is.available();
                    if (bytesRead>2048){
                        bytesRead=2048;
                    }
                    totalBytes += bytesRead;
                    is.read(buffer, 0, bytesRead);
                    fos.write(buffer, 0 , bytesRead);

                    int percent = (int)(((float)totalBytes/(float)length)*100);
                    if (percent!= 0 && (percent-oldPercent > 5 || percent==100))
                    {
                        Log.i(TAG, "Downloading " + percent + " %");
                        oldPercent = percent;
                    }

                }
                fos.flush();
                fos.close();

                file.setReadable(true,false);
                downloadDoneListener.onDownloadDone(downloadRequest);
                
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.d(TAG, "Error: " + e);
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
        super.onCreate();
        Log.i(TAG,"onCreate download service");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"Download Service::onStartCommand ");
        DownloadRequest downloadRequest = (DownloadRequest)intent.getSerializableExtra(DownloadRequest.INTENT_EXTRA_KEY);
        Log.i(TAG,"Path= "+downloadRequest.path+"   URL "+downloadRequest.url);

        download(downloadRequest,new OnDownloadDoneListener() {
             public void onDownloadDone(DownloadRequest downloadRequest) {

                 Intent i = new Intent();
                 i.setClassName("com.samsung.installersystemservice"
                               ,"com.samsung.installersystemservice.DownloadDoneBroadcastReceiver");
                 i.putExtra(DownloadRequest.INTENT_EXTRA_KEY,downloadRequest);
                 sendBroadcast(i);

             }
        });
        return  0;
    }


}
