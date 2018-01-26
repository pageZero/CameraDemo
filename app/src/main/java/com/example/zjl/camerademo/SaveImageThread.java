package com.example.zjl.camerademo;

import android.os.HandlerThread;
import android.os.Handler;
import android.os.Message;
import android.os.Looper;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.lang.ref.SoftReference;

import android.provider.MediaStore;

import android.content.Intent;

import android.net.Uri;
import android.util.Log;

/**
 * Created by zjl on 18-1-4.
 * 保存图片
 */

public class SaveImageThread extends HandlerThread {

    private String savePath = CameraApplication.sAppContext.getFilesDir().getAbsolutePath();
    private CameraActivity cameraActivity;
    private Handler mSaveImageHandler;

    public SaveImageThread(String name) {
        super(name);
    }

    public SaveImageThread(String name, int priority) {
        super(name, priority);
    }

    public SaveImageThread(String name, CameraActivity activity, String savePath) {
        super(name);
        cameraActivity = new SoftReference<CameraActivity>(activity).get();
        this.savePath = savePath;
    }

    public SaveImageThread(String name, CameraActivity activity) {
        super(name);
        cameraActivity = new SoftReference<CameraActivity>(activity).get();
    }

    public void initHandler(Looper looper) {
        mSaveImageHandler = new Handler(looper) {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case R.id.save_image:
                        saveImage((byte[]) msg.obj);
                        if (cameraActivity != null) {
                            cameraActivity.showToast(CameraApplication.sAppContext.getString(R.string.save_success));
                        }
                        break;
                    case R.id.quit:
                        Looper looper = Looper.myLooper();
                        Log.d("SaveImageThread","lopper--thread name:" + looper.getThread().getName());
                        if (looper != null) {
                            looper.quit();
                        }
                }
            }
        };
    }

    private void saveImage(byte[] imageData) {
        //保存图片
        String fileName = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(new Date()) + ".jpg";
        File imageFile = new File(savePath, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);
            fos.write(imageData);
            fos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //将图片插入系统图库
        try {
            MediaStore.Images.Media.insertImage(CameraApplication.sAppContext.getContentResolver(),
                    imageFile.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // 最后通知图库更新
        CameraApplication.sAppContext.sendBroadcast(
                new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.fromFile(imageFile)));
    }

    public Handler getHandler() {
        return mSaveImageHandler;
    }

}
