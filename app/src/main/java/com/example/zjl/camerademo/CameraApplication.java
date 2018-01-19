package com.example.zjl.camerademo;

import android.app.Application;
import android.content.Context;
/**
 * Created by zjl on 18-1-3.
 */

public class CameraApplication extends Application{

    public static Context sAppContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sAppContext = this;
    }
}
