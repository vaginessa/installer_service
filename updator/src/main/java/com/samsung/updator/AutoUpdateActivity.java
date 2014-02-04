package com.samsung.updator;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.samsung.updator.apihelper.ApplicationManager;
import com.samsung.updator.apihelper.OnInstalledPackaged;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class AutoUpdateActivity extends Activity {

    static final String TAG = "AutoUpdateActivity";
    private RequestQueue queue;
    private SharedPreferences sharedPreferences;
    private ProgressDialog dialog;
    private ApplicationManager am;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_update);

        try {
            am = new ApplicationManager(AutoUpdateActivity.this);
            am.setOnInstalledPackaged(new OnInstalledPackaged() {

                public void packageInstalled(String packageName, int returnCode) {
                    if (returnCode == ApplicationManager.INSTALL_SUCCEEDED) {
                        Log.d(TAG, "Install succeeded");
                    } else {
                        Log.d(TAG, "Install failed: " + returnCode);
                    }
                }
            });

            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

            queue = Volley.newRequestQueue(this);
            Button btnDownlaod = (Button) findViewById(R.id.btnDownload);
            btnDownlaod.setOnClickListener(new View.OnClickListener() {public void onClick(View arg0) {

                setDialogMessage("Checking Update");

                JsonObjectRequest request = new JsonObjectRequest(
                        JsonObjectRequest.Method.GET,
                        "http://oep.app-friday.com/json/doUpdate.json",
                        null,
                        new Response.Listener<JSONObject>(){public void onResponse(final JSONObject response) {
                            runOnUiThread(new Runnable() {public void run() {synchronized(AutoUpdateActivity.this){
                                try {
                                    double version = sharedPreferences.getFloat("version", 0);
                                    double serverVersion = response.getDouble("version");
                                    String url = response.getString("url");
                                    if (version < serverVersion) {
                                        downloadAndInstallApk(url);
                                    }
                                    else {
                                        dismissDialog("Already Up To Date");
                                    }
                                } catch (JSONException e) {
                                    dismissDialog("JSON ERROR");
                                }

                            }}});
                        }},
                        new Response.ErrorListener() {public void onErrorResponse(VolleyError error) {
                            dismissDialog("ERROR DOWNLOADING");
                        }
                });
                queue.add(request);
            }});

            Button btnInstall = (Button) findViewById(R.id.btnInstall);
            btnInstall.setOnClickListener(new View.OnClickListener() {public void onClick(View arg0) {
                try {
                    File root = android.os.Environment.getExternalStorageDirectory();

                    File extStore = new File ("/sdcard/","the_file.apk");
                    am.installPackage(extStore);
                } catch (Exception e) {
                    logError(e);
                }
            }});

        } catch (Exception e) {
            logError(e);
        }
    }

    private void logError(Exception e) {
        e.printStackTrace();
        Toast.makeText(AutoUpdateActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
    }

    private void downloadAndInstallApk(final String DownloadUrl){
        setDialogMessage("Downloading, Please Wait....");
        new Thread(new Runnable(){public void run() {

            try {
                File root = android.os.Environment.getExternalStorageDirectory();
            //    String appPath = getApplicationContext().getFilesDir().getAbsolutePath();

                URL url = new URL(DownloadUrl);
                File file = new File(root, "prod--oep-release.apk");
                Log.i(TAG,"Downloading "+DownloadUrl+" --> "+file);

                long startTime = System.currentTimeMillis();

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

                    int percent = (int)(((float)totalBytes/(float)length)*100);
                    setDialogMessage("Downloading. " + percent + " %");
                    is.read(buffer, 0, bytesRead);
                    fos.write(buffer);
                }
                fos.flush();
                fos.close();
                //Log.d("DownloadManager", "download ready in" + ((System.currentTimeMillis() - startTime) / 1000) + " sec");

                setDialogMessage("Installing. ");
                try {
                    am.installPackage(file);
                    dismissDialog("Successfully Installed!");
                } catch (Exception e) {
                    dismissDialog(e.getLocalizedMessage());
                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                Log.d(TAG, "Error: " + e);
            }

        }}).start();
    }

    void setDialogMessage(final String message){
        runOnUiThread(new Runnable() {public void run() {synchronized(AutoUpdateActivity.this){
            if (dialog == null){
                dialog = ProgressDialog.show(AutoUpdateActivity.this, "",message, false);
            }
            else {
                dialog.setMessage(message);
            }
        }}});

    }

    void dismissDialog(final String message){
        runOnUiThread(new Runnable() {public void run() {synchronized(AutoUpdateActivity.this){
            if (message!=null){
                Toast.makeText(AutoUpdateActivity.this,message,Toast.LENGTH_LONG).show();
            }
            dialog.dismiss();
            dialog = null;
        }}});
    }
}