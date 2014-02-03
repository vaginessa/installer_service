package com.samsung.models;

import java.io.Serializable;

/**
 * Created by mkluver on 1/29/14.
 */
final public class DownloadRequest implements Serializable {
    public static final String INTENT_EXTRA_KEY = "DownloadRequest";

    public final String url;
    public final String path;
    public DownloadRequest(String url,String path){
        this.url = url;
        this.path = path;
    }
}
