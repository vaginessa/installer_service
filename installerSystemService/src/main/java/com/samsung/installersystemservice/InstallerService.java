package com.samsung.installersystemservice;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.samsung.installersystemservice.apihelper.ApplicationManager;
import com.samsung.installersystemservice.apihelper.OnInstalledPackaged;
import com.samsung.models.DownloadRequest;

import org.json.JSONException;
import org.json.JSONObject;

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
public class InstallerService extends IntentService  {
    private static final String TAG = "MARC";

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private RequestQueue queue;

    private ApplicationManager am;


    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(final Message msg) {
            Log.i(TAG,"installer handling message");

            File root = android.os.Environment.getExternalStorageDirectory();
            String appPath = getApplicationContext().getFilesDir().getAbsolutePath();
            Log.i(TAG, "SYS root=" + root + "           appPath=" + appPath);


            JsonObjectRequest request = new JsonObjectRequest(
                    JsonObjectRequest.Method.GET,
                    "http://oep.app-friday.com/json/doUpdate.json",
                    null,
                    new Response.Listener<JSONObject>(){
                        public void onResponse(final JSONObject response) {
                            new Thread(new Runnable(){public void run() {
                            try {
                               // double version = sharedPreferences.getFloat("version", 0);
                               // double serverVersion = response.getDouble("version");
                               String url = response.getString("url");
                               // if (version < serverVersion) {

                           //     Intent i = new Intent();
                            //    i.setClassName("com.samsung.downloadservice","com.samsung.downloadservice.DownloadRequestBroadcastReceiver");

                                File root = getApplicationContext().getFilesDir();//android.os.Environment.getExternalStorageDirectory();
                                File apkOnSdCard = new File(root,"the_file.apk");
                                DownloadRequest downloadRequest = new DownloadRequest(url,apkOnSdCard.getAbsolutePath());

                                //i.putExtra(DownloadRequest.INTENT_EXTRA_KEY,downloadRequest);
                                //sendBroadcast(i);

                                download(downloadRequest,new OnDownloadDoneListener() {
                                    public void onDownloadDone(DownloadRequest downloadRequest) {
                                        try {
                                            am = new ApplicationManager(getApplicationContext());

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
                                });

                                // Stop the service using the startId, so that we don't stop
                                // the service in the middle of handling another job
                                stopSelf(msg.arg1);

                                // }
                               // else {
                               //     dismissDialog("Already Up To Date");
                               // }
                            } catch (JSONException e) {
                               // dismissDialog("JSON ERROR");
                                Log.e(TAG,"ERROR "+e.getLocalizedMessage());
                            }
                            }}).start();

                        }},
                    new Response.ErrorListener() {public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "ERROR DOWNLOADING "+error.getLocalizedMessage());
                    }});
            queue.add(request);
        }
    }

    interface OnDownloadDoneListener {
        public void onDownloadDone(DownloadRequest downloadRequest);
    }

    private void download(final DownloadRequest downloadRequest, final OnDownloadDoneListener downloadDoneListener){
     //   new Thread(new Runnable(){public void run() {
            try {
                File root = getApplicationContext().getFilesDir();//android.os.Environment.getExternalStorageDirectory();
                //File root = android.os.Environment.getExternalStorageDirectory();
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
      //  }}).start();
    }

    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        Log.i(TAG,"onCreate installer service");

        queue = Volley.newRequestQueue(this);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG,"onStartCommand installer service");

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }


    /**
     * Creates an DownloadService.  Invoked by your subclass's constructor.
     *
     */
    public InstallerService() {
        super("Installer Service");

        Log.i(TAG,"Constructing installer service");



    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG,"Handling Intent for installer service");



        Intent downloadIntent = new Intent();
        downloadIntent.setClassName("com.samsung.installersystemservice",  "com.samsung.installersystemservice.SystemStartupBroadcastReceiver");
        startService(downloadIntent);
    }
}
