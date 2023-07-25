package com.socket.http_cs.cronet.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import org.chromium.net.CronetEngine;
public class ChromiumLibraryLoader extends CronetEngine.Builder.LibraryLoader {

    private static final String TAG = "ChromiumLibraryLoader";

    private Context context;

    public ChromiumLibraryLoader(Context context) {
        this.context = context.getApplicationContext();
    }

    @SuppressLint("UnsafeDynamicallyLoadedCode")
    @Override
    public void loadLibrary(String libName) {
        Log.w(TAG, "libName:" + libName);
        long start = System.currentTimeMillis();
        try {
            //非cronet的so调用系统方法加载
            if (!libName.contains("cronet")) {
                System.loadLibrary(libName);
                return;
            }
            //加载本地cronet
            System.loadLibrary(libName);
            Log.w(TAG, "load from system");
        } catch (Throwable e) {
            Log.w(TAG, "print error");
            e.printStackTrace();
        } finally {
            Log.w(TAG, "time:" + (System.currentTimeMillis() - start));
        }
    }

}
