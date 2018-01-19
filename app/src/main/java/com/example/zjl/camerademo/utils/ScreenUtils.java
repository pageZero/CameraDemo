package com.example.zjl.camerademo.utils;

import android.content.Context;
import android.util.DisplayMetrics;

import com.example.zjl.camerademo.CameraApplication;

/**
 * Created by zjl on 18-1-3.
 */

public class ScreenUtils {

    /**
     * 获取屏幕宽度
     *
     * @return
     */
    public static int getScreenWidth() {
        Context context = CameraApplication.sAppContext;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    /**
     * 获取屏幕高度
     *
     * @return
     */
    public static int getScreenHeight() {
        Context context = CameraApplication.sAppContext;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }
}
